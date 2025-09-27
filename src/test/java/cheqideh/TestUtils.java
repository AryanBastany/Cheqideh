package cheqideh;

import cheqideh.config.JwtUtil;
import cheqideh.dto.request.AddAccountRequest;
import cheqideh.dto.request.IssueChequeRequest;
import cheqideh.model.account.Account;
import cheqideh.model.account.AccountStatus;
import cheqideh.model.cheque.Cheque;
import cheqideh.model.cheque.ChequeStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TestUtils {
    public static Account generateRandomAccount() {
        Random random = new Random();
        Account account = new Account();
        account.setId(generateRandomLong(0L));
        account.setAccId(generateRandomLong(0L));
        account.setBalance(BigDecimal.valueOf(generateRandomLong(0L)));

        AccountStatus[] statuses = AccountStatus.values();
        int randomIndex = random.nextInt(statuses.length);
        account.setStatus(statuses[randomIndex]);

        return account;
    }

    public static Account generateValidAccount() {
        Account account = generateRandomAccount();
        account.setStatus(AccountStatus.ACTIVE);

        return account;
    }

    public static Cheque generateRandomCheque(Account drawer) {
        Random random = new Random();
        Cheque cheque = new Cheque();
        cheque.setId(generateRandomLong(0L));
        cheque.setDrawer(drawer);
        cheque.setAmount(BigDecimal.valueOf(generateRandomLong(0L)));
        cheque.setNumber(generateRandomString(100));
        cheque.setIssueDate(generateRandomDate(LocalDate.of(
                2000, 1, 1), LocalDate.now()));

        ChequeStatus[] statuses = ChequeStatus.values();
        int randomIndex = random.nextInt(statuses.length);
        cheque.setStatus(statuses[randomIndex]);

        return cheque;
    }

    public static Cheque generateValidCheque(Account drawer) {
        Cheque cheque = generateRandomCheque(drawer);

        long drawerBalance = Long.parseLong(drawer.getBalance().toString());
        long chequeAmount = TestUtils.generateRandomLong(0L, drawerBalance + 1);
        cheque.setAmount(BigDecimal.valueOf(chequeAmount));

        cheque.setIssueDate(LocalDate.now());

        cheque.setStatus(ChequeStatus.ISSUED);

        return cheque;
    }

    public static IssueChequeRequest createIssueRequest(long drawerId, BigDecimal amount) {
        IssueChequeRequest request = new IssueChequeRequest();
        request.setDrawerId(drawerId);
        request.setAmount(amount);
        request.setNumber(generateRandomString(20));

        return request;
    }

    public static AddAccountRequest createAddAccountRequest(long accId, BigDecimal balance) {
        AddAccountRequest request = new AddAccountRequest();
        request.setAccId(accId);
        request.setBalance(balance);

        return request;
    }

    public static String generateRandomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for(int i = 0; i < length; i++) {
            char c = (char) ('a' + random.nextInt(26));
            if (i == 0) c = Character.toUpperCase(c); // Capitalize first letter
            sb.append(c);
        }
        return sb.toString();
    }

    public static LocalDate generateRandomDate(LocalDate startDate, LocalDate endDate) {
        long startEpochDay = startDate.toEpochDay();
        long endEpochDay = endDate.toEpochDay();

        long randomEpochDay = ThreadLocalRandom.current()
                .nextLong(startEpochDay, endEpochDay + 1);

        return LocalDate.ofEpochDay(randomEpochDay);
    }

    public static Long generateRandomLong(Long from, Long to) {
        return ThreadLocalRandom.current().nextLong(from, to);
    }

    public static Long generateRandomLong(Long from) {
        return ThreadLocalRandom.current().nextLong(from, Long.MAX_VALUE);
    }

    public static Integer generateRandomInt(Integer from, Integer to) {
        Random random = new Random();
        return random.nextInt(from, to);
    }

    public static String generateTellerToken(JwtUtil jwtUtil) {
        return jwtUtil.generateToken("teller1", List.of("TELLER"));
    }
}
