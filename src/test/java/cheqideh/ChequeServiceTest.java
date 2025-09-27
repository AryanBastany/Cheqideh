package cheqideh;

import cheqideh.client.SayadClient;
import cheqideh.exception.servicelayer.*;
import cheqideh.model.BounceRecord;
import cheqideh.model.account.Account;
import cheqideh.model.account.AccountStatus;
import cheqideh.model.cheque.Cheque;
import cheqideh.model.cheque.ChequeStatus;
import cheqideh.service.business.ChequeService;
import cheqideh.service.crud.AccountCrudService;
import cheqideh.service.crud.BounceRecCrudService;
import cheqideh.service.crud.ChequeCrudService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChequeServiceTest {

    @Mock
    private AccountCrudService accountCrudService;
    @Mock
    private ChequeCrudService chequeCrudService;
    @Mock
    private BounceRecCrudService bounceRecordCrudService;
    @Mock
    private SayadClient sayadClient;

    @InjectMocks
    private ChequeService chequeService;

    @Test
    @DisplayName("issueCheque should succeed when all validations pass")
    void issueCheque_IssuesCorrectly_IfEverythingIsCorrect() throws Exception {
        Account drawer = TestUtils.generateValidAccount();

        Cheque newCheque = TestUtils.generateValidCheque(drawer);

        Cheque result = chequeService.issueCheque(newCheque);

        assertNotNull(result);
        verify(sayadClient, times(1)).registerCheque();
        verify(chequeCrudService, times(1)).add(newCheque);
    }

    @Test
    @DisplayName("issueCheque should fail with InsufficientFundsException when balance is too low")
    void issueCheque_Fails_IfBalanceIsInsufficient() {
        long drawerBalance = TestUtils.generateRandomLong(0L, Long.MAX_VALUE - 1);
        Account drawer = TestUtils.generateValidAccount();
        drawer.setBalance(BigDecimal.valueOf(drawerBalance));

        long chequeAmount = TestUtils.generateRandomLong(drawerBalance + 1, Long.MAX_VALUE);
        Cheque newCheque = TestUtils.generateValidCheque(drawer);
        newCheque.setAmount(BigDecimal.valueOf(chequeAmount));


        InsufficientFundsException thrown = assertThrows(InsufficientFundsException.class, () -> {
            chequeService.issueCheque(newCheque);
        });
        assertEquals("Insufficient funds to issue the cheque.", thrown.getMessage());
        verify(sayadClient, never()).registerCheque();
    }

    @Test
    @DisplayName("issueCheque should fail with AccountBlockedException when account is blocked")
    void issueCheque_Fails_IfAccountIsBlocked() {
        Account drawer = TestUtils.generateValidAccount();
        drawer.setStatus(AccountStatus.BLOCKED);

        Cheque newCheque = TestUtils.generateValidCheque(drawer);

        AccountBlockedException thrown = assertThrows(AccountBlockedException.class, () -> {
            chequeService.issueCheque(newCheque);
        });
        assertEquals("Account is blocked and cannot issue cheques.", thrown.getMessage());
        verify(sayadClient, never()).registerCheque();
    }

    @Test
    @DisplayName("presentCheque should succeed when cheque is valid and funds are sufficient")
    void presentCheque_PresentsCorrectly_IfValidAndSufficientFunds() {
        long drawerBalance = TestUtils.generateRandomLong(1L);
        Account drawer = TestUtils.generateValidAccount();
        drawer.setBalance(BigDecimal.valueOf(drawerBalance));

        long chequeAmount = TestUtils.generateRandomLong(1L, drawerBalance + 1);
        Cheque newCheque = TestUtils.generateValidCheque(drawer);
        newCheque.setAmount(BigDecimal.valueOf(chequeAmount));

        when(chequeCrudService.findById(newCheque.getId())).thenReturn(newCheque);

        chequeService.presentCheque(newCheque.getId());

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        ArgumentCaptor<Cheque> chequeCaptor = ArgumentCaptor.forClass(Cheque.class);
        verify(sayadClient, times(1)).presentCheque();
        verify(accountCrudService, times(1)).add(accountCaptor.capture());
        verify(chequeCrudService, times(1)).add(chequeCaptor.capture());

        BigDecimal asd = accountCaptor.getValue().getBalance();
        assertEquals(0, new BigDecimal(drawerBalance - chequeAmount).compareTo(accountCaptor.getValue().getBalance()));
        assertEquals(ChequeStatus.PAID, chequeCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("presentCheque should fail with ChequeExpiredException when cheque is expired")
    void presentCheque_Fails_IfChequeIsExpired() {
        Cheque expiredCheque = TestUtils.generateValidCheque(TestUtils.generateRandomAccount());
        expiredCheque.setIssueDate(TestUtils.generateRandomDate(LocalDate.now().minusYears(10),
                LocalDate.now().minusMonths(6).minusDays(1)));

        when(chequeCrudService.findById(expiredCheque.getId())).thenReturn(expiredCheque);

        ChequeExpiredException thrown = assertThrows(ChequeExpiredException.class, () -> chequeService.presentCheque(expiredCheque.getId()));
        assertEquals("Cheque is expired and cannot be presented.", thrown.getMessage());
        verify(sayadClient, never()).presentCheque();
    }

    @Test
    @DisplayName("presentCheque should fail with ChequeStatusException when status is not ISSUED")
    void presentCheque_Fails_IfChequeStatusIsNotIssued() {
        Cheque paidCheque = TestUtils.generateValidCheque(TestUtils.generateRandomAccount());
        paidCheque.setStatus(ChequeStatus.PAID);

        when(chequeCrudService.findById(paidCheque.getId())).thenReturn(paidCheque);

        ChequeStatusException thrown = assertThrows(ChequeStatusException.class, () -> chequeService.presentCheque(paidCheque.getId()));
        assertEquals("Cheque is not in ISSUED state.", thrown.getMessage());
        verify(sayadClient, never()).presentCheque();
    }

    @Test
    @DisplayName("presentCheque should BOUNCE and BLOCK account on the 3rd bounce")
    void presentCheque_BouncesAndBlocks_IfInsufficientFundsAndHighBounceCount() {
        long drawerBalance = TestUtils.generateRandomLong(0L, Long.MAX_VALUE - 1);
        Account drawer = TestUtils.generateValidAccount();
        drawer.setBalance(BigDecimal.valueOf(drawerBalance));

        long chequeAmount = TestUtils.generateRandomLong(drawerBalance + 1, Long.MAX_VALUE);
        Cheque cheque = TestUtils.generateValidCheque(drawer);
        cheque.setAmount(BigDecimal.valueOf(chequeAmount));

        when(chequeCrudService.findById(cheque.getId())).thenReturn(cheque);
        when(bounceRecordCrudService.countBounceDateAfter(eq(drawer.getId()), any(LocalDate.class))).thenReturn(3L);

        ChequeBounceException thrown = assertThrows(ChequeBounceException.class, () -> chequeService.presentCheque(cheque.getId()));
        assertEquals("Cheque bounced due to insufficient funds.", thrown.getMessage());

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountCrudService, times(1)).add(accountCaptor.capture());
        assertEquals(AccountStatus.BLOCKED, accountCaptor.getValue().getStatus());
        verify(bounceRecordCrudService, times(1)).add(any(BounceRecord.class));
    }

    @Test
    @DisplayName("presentCheque should BOUNCE without blocking when bounce count is low")
    void presentCheque_BouncesWithoutBlocking_IfInsufficientFundsAndLowBounceCount() {
        long drawerBalance = TestUtils.generateRandomLong(0L, Long.MAX_VALUE - 1);
        Account drawer = TestUtils.generateValidAccount();
        drawer.setBalance(BigDecimal.valueOf(drawerBalance));

        long chequeAmount = TestUtils.generateRandomLong(drawerBalance + 1, Long.MAX_VALUE);
        Cheque cheque = TestUtils.generateValidCheque(drawer);
        cheque.setAmount(BigDecimal.valueOf(chequeAmount));

        when(chequeCrudService.findById(cheque.getId())).thenReturn(cheque);
        when(bounceRecordCrudService.countBounceDateAfter(eq(drawer.getId()), any(LocalDate.class))).thenReturn(2L);

        ChequeBounceException thrown = assertThrows(ChequeBounceException.class, () -> chequeService.presentCheque(cheque.getId()));
        assertEquals("Cheque bounced due to insufficient funds.", thrown.getMessage());

        verify(accountCrudService, never()).add(any());
        verify(bounceRecordCrudService, times(1)).add(any(BounceRecord.class));
        verify(chequeCrudService, times(1)).add(argThat(c -> c.getStatus() == ChequeStatus.BOUNCED));
    }
}