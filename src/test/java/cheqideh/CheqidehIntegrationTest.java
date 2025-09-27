package cheqideh;

import cheqideh.client.SayadClient;
import cheqideh.config.JwtUtil;
import cheqideh.dto.request.AddAccountRequest;
import cheqideh.dto.request.IssueChequeRequest;
import cheqideh.model.account.Account;
import cheqideh.model.account.AccountStatus;
import cheqideh.model.cheque.ChequeStatus;
import cheqideh.service.crud.AccountCrudService;
import cheqideh.service.crud.ChequeCrudService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.ResultActions;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CheqidehIntegrationTest {

    private static final String ACCOUNTS_API_PATH = "/api/accounts";
    private static final String CHEQUES_API_BASE_PATH = "/api/cheques";
    private static final String PRESENT_CHEQUE_API_PATH = CHEQUES_API_BASE_PATH + "/{chequeId}/present";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AccountCrudService accountCrudService;
    @Autowired
    private ChequeCrudService chequeCrudService;
    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private SayadClient sayadClient;

    private String tellerToken;

    @BeforeEach
    void setUp() {
        tellerToken = TestUtils.generateTellerToken(jwtUtil);

        when(sayadClient.registerCheque()).thenReturn(ResponseEntity.ok().build());
        when(sayadClient.presentCheque()).thenReturn(ResponseEntity.ok().build());
    }

    @Test
    @DisplayName("issueAndPresent should succeed when funds are sufficient")
    void issueAndPresent_Succeeds_IfFundsAreSufficient() throws Exception {
        long initialBalance = TestUtils.generateRandomLong(1L);
        long accId = TestUtils.generateRandomLong(0L);
        AddAccountRequest addAccReq = TestUtils.createAddAccountRequest(accId, BigDecimal.valueOf(initialBalance));
        performAddAccount(addAccReq);

        long chequeAmount = TestUtils.generateRandomLong(1L, initialBalance + 1);
        long expectedFinalBalance = initialBalance - chequeAmount;
        IssueChequeRequest issueRequest = TestUtils.createIssueRequest(accId, BigDecimal.valueOf(chequeAmount));

        long chequeId = performIssueCheque(issueRequest);
        performPresentCheque(chequeId)
                .andExpect(status().isOk());

        var paidCheque = chequeCrudService.findById(chequeId);
        assertEquals(ChequeStatus.PAID, paidCheque.getStatus());

        var updatedAccount = accountCrudService.findById(accId);
        assertEquals(0, new BigDecimal(expectedFinalBalance).compareTo(updatedAccount.getBalance()));
    }

    @Test
    @DisplayName("presentCheque should fail when funds are insufficient")
    void presentCheque_Fails_IfFundsAreInsufficient() throws Exception {
        long initialBalance = TestUtils.generateRandomLong(0L);
        long accId = TestUtils.generateRandomLong(0L);
        AddAccountRequest addAccReq = TestUtils.createAddAccountRequest(accId, BigDecimal.valueOf(initialBalance));
        performAddAccount(addAccReq);

        long validChequeAmount = TestUtils.generateRandomLong(1L, initialBalance);
        IssueChequeRequest issueRequest = TestUtils.createIssueRequest(accId, BigDecimal.valueOf(validChequeAmount));
        long chequeId = performIssueCheque(issueRequest);

        BigDecimal insufficientBalance = BigDecimal.valueOf(TestUtils.generateRandomLong(0L, validChequeAmount));
        Account changedAccount = accountCrudService.findById(accId);
        changedAccount.setBalance(insufficientBalance);
        accountCrudService.add(changedAccount);

        performPresentCheque(chequeId)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cheque bounced."));
    }

    @Test
    @DisplayName("presentCheque should block account on the third bounce")
    void presentCheque_BlocksAccount_OnThirdBounce() throws Exception {
        long initialBalance = 1_000_000;
        long accId = TestUtils.generateRandomLong(0L);
        AddAccountRequest addAccReq = TestUtils.createAddAccountRequest(accId, BigDecimal.valueOf(initialBalance));
        performAddAccount(addAccReq);

        List<Long> chequeIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            IssueChequeRequest request = TestUtils.createIssueRequest(accId, BigDecimal.valueOf(100_000));
            chequeIds.add(performIssueCheque(request));
        }

        Account changedAccount = accountCrudService.findById(accId);
        changedAccount.setBalance(BigDecimal.valueOf(100));
        accountCrudService.add(changedAccount);

        for (long chequeId : chequeIds) {
            performPresentCheque(chequeId)
                    .andExpect(status().isConflict());
        }

        var blockedAccount = accountCrudService.findById(accId);
        assertEquals(AccountStatus.BLOCKED, blockedAccount.getStatus());
    }

    private long performIssueCheque(IssueChequeRequest request) throws Exception {
        String responseBody = mockMvc.perform(post(CHEQUES_API_BASE_PATH)
                        .header("Authorization", "Bearer " + tellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseBody).get("id").asLong();
    }

    private ResultActions performPresentCheque(long chequeId) throws Exception {
        return mockMvc.perform(post(PRESENT_CHEQUE_API_PATH, chequeId)
                .header("Authorization", "Bearer " + tellerToken));
    }

    private void performAddAccount(AddAccountRequest request) throws Exception {
        mockMvc.perform(post(ACCOUNTS_API_PATH)
                .header("Authorization", "Bearer " + tellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}