package Cheqideh.service.business;

import Cheqideh.client.SayadClient;
import Cheqideh.dto.request.IssueChequeRequest;
import Cheqideh.exception.servicelayer.ChequeBounceException;
import Cheqideh.model.BounceRecord;
import Cheqideh.model.account.Account;
import Cheqideh.model.account.AccountStatus;
import Cheqideh.model.cheque.Cheque;
import Cheqideh.model.cheque.ChequeStatus;
import Cheqideh.service.crud.AccountCrudService;
import Cheqideh.service.crud.BounceRecCrudService;
import Cheqideh.service.crud.ChequeCrudService;
import Cheqideh.validator.servicelayer.ChequeServiceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ChequeService {
    private final AccountCrudService accountCrudService;
    private final ChequeCrudService chequeCrudService;
    private final BounceRecCrudService bounceRecordCrudService;

    private final SayadClient sayadClient;

    private static final int MAX_BOUNCES_PER_YEAR = 3;

    @Transactional
    public Cheque issueCheque(Cheque newCheque) throws AccountNotFoundException {
        ChequeServiceValidator.validateCheque(newCheque);

        sayadClient.registerCheque();

        chequeCrudService.add(newCheque);
        return newCheque;
    }

    @Transactional
    public Cheque issueCheque(IssueChequeRequest request) throws AccountNotFoundException {
        Account drawer = accountCrudService.findById(request.getDrawerId());

        Cheque newCheque = new Cheque();
        newCheque.setDrawer(drawer);
        newCheque.setNumber(request.getNumber());
        newCheque.setAmount(request.getAmount());
        newCheque.setIssueDate(LocalDate.now());
        newCheque.setStatus(ChequeStatus.ISSUED);

        return issueCheque(newCheque);
    }

    @Transactional
    public void presentCheque(Long chequeId) {
        Cheque cheque = chequeCrudService.findById(chequeId);

        ChequeServiceValidator.validatePresent(cheque, chequeId);

        sayadClient.presentCheque();

        if (hasSufficientFunds(cheque)) {
            processSuccessfulPayment(cheque);
        } else {
            handleBouncedCheque(cheque);
        }
    }

    private boolean hasSufficientFunds(Cheque cheque) {
        return cheque.getDrawer().getBalance().compareTo(cheque.getAmount()) >= 0;
    }

    private void processSuccessfulPayment(Cheque cheque) {
        Account drawer = cheque.getDrawer();
        drawer.setBalance(drawer.getBalance().subtract(cheque.getAmount()));
        cheque.setStatus(ChequeStatus.PAID);

        accountCrudService.add(drawer);
        chequeCrudService.add(cheque);
    }

    private void handleBouncedCheque(Cheque cheque) {
        cheque.setStatus(ChequeStatus.BOUNCED);
        chequeCrudService.add(cheque);

        BounceRecord bounce = new BounceRecord();
        bounce.setCheque(cheque);
        bounce.setBounceDate(LocalDate.now());
        bounce.setReason("INSUFFICIENT_FUNDS");

        bounceRecordCrudService.add(bounce);

        if (shouldBlockAccount(cheque.getDrawer())) {
            blockAccount(cheque.getDrawer());
        }

        throw new ChequeBounceException("Cheque bounced due to insufficient funds.");
    }

    private boolean shouldBlockAccount(Account drawer) {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        long bounceCount = bounceRecordCrudService.countBounceDateAfter(drawer.getId(), oneYearAgo);
        return bounceCount >= MAX_BOUNCES_PER_YEAR;
    }

    private void blockAccount(Account drawer) {
        drawer.setStatus(AccountStatus.BLOCKED);
        accountCrudService.add(drawer);
    }
}