package cheqideh;

import Cheqideh.client.SayadClient;
import cheqideh.TestUtils;
import Cheqideh.config.JwtUtil;
import Cheqideh.dto.request.IssueChequeRequest;
import Cheqideh.model.account.Account;
import Cheqideh.model.account.AccountStatus;
import Cheqideh.model.cheque.ChequeStatus;
import Cheqideh.service.crud.AccountCrudService;
import Cheqideh.service.crud.ChequeCrudService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
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
    private Account testAccount;

    @BeforeEach
    void setUp() {
        tellerToken = TestUtils.generateTellerToken(jwtUtil);
        testAccount = TestUtils.generateValidAccount();
        testAccount = accountCrudService.add(testAccount);

        when(sayadClient.registerCheque()).thenReturn(ResponseEntity.ok().build());
        when(sayadClient.presentCheque()).thenReturn(ResponseEntity.ok().build());
    }

    @Test
    @DisplayName("issueAndPresent should succeed when funds are sufficient")
    void issueAndPresent_Succeeds_IfFundsAreSufficient() throws Exception {
        long initialBalance = TestUtils.generateRandomLong(1L);
        testAccount.setBalance(BigDecimal.valueOf(initialBalance));
        accountCrudService.add(testAccount);

        long chequeAmount = TestUtils.generateRandomLong(1L, initialBalance + 1);
        long expectedFinalBalance = initialBalance - chequeAmount;
        IssueChequeRequest issueRequest = TestUtils.createIssueRequest(testAccount.getId(), BigDecimal.valueOf(chequeAmount));

        long chequeId = performIssueCheque(issueRequest);
        performPresentCheque(chequeId)
                .andExpect(status().isOk());

        var paidCheque = chequeCrudService.findById(chequeId);
        assertEquals(ChequeStatus.PAID, paidCheque.getStatus());

        var updatedAccount = accountCrudService.findById(testAccount.getId());
        assertEquals(0, new BigDecimal(expectedFinalBalance).compareTo(updatedAccount.getBalance()));
    }

    @Test
    @DisplayName("presentCheque should fail when funds are insufficient")
    void presentCheque_Fails_IfFundsAreInsufficient() throws Exception {
        long validChequeAmount = TestUtils.generateRandomLong(1L, testAccount.getBalance().longValue());
        IssueChequeRequest issueRequest = TestUtils.createIssueRequest(testAccount.getId(), BigDecimal.valueOf(validChequeAmount));
        long chequeId = performIssueCheque(issueRequest);

        BigDecimal insufficientBalance = BigDecimal.valueOf(TestUtils.generateRandomLong(0L, validChequeAmount));
        testAccount.setBalance(insufficientBalance);
        accountCrudService.add(testAccount);

        performPresentCheque(chequeId)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cheque bounced."));
    }

    @Test
    @DisplayName("presentCheque should block account on the third bounce")
    void presentCheque_BlocksAccount_OnThirdBounce() throws Exception {
        testAccount.setBalance(BigDecimal.valueOf(1_000_000));
        accountCrudService.add(testAccount);

        List<Long> chequeIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            IssueChequeRequest request = TestUtils.createIssueRequest(testAccount.getId(), BigDecimal.valueOf(100_000));
            chequeIds.add(performIssueCheque(request));
        }

        testAccount.setBalance(BigDecimal.valueOf(100));
        accountCrudService.add(testAccount);

        for (long chequeId : chequeIds) {
            performPresentCheque(chequeId)
                    .andExpect(status().isConflict());
        }

        var blockedAccount = accountCrudService.findById(testAccount.getId());
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
}