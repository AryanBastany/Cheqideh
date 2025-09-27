package cheqideh.validator.servicelayer;

import cheqideh.exception.servicelayer.*;
import cheqideh.model.account.Account;
import cheqideh.model.account.AccountStatus;
import cheqideh.model.cheque.Cheque;
import cheqideh.model.cheque.ChequeStatus;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDate;

public class ChequeServiceValidator {
    public static void validateCheque(Cheque cheque) throws AccountNotFoundException {
        Account drawer = cheque.getDrawer();
        if (drawer == null) {
            throw new AccountNotFoundException("Drawer account not found.");
        }

        if (drawer.getStatus() == AccountStatus.BLOCKED) {
            throw new AccountBlockedException("Account is blocked and cannot issue cheques.");
        }
        if (drawer.getBalance().compareTo(cheque.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds to issue the cheque.");
        }
    }

    public static void validatePresent(Cheque cheque, Long chequeId) {
        if (cheque == null) {
            throw new ChequeNotFoundException("Cheque not found with ID: " + chequeId);
        }
        Account drawer = cheque.getDrawer();

        if (cheque.getStatus() != ChequeStatus.ISSUED) {
            throw new ChequeStatusException("Cheque is not in ISSUED state.");
        }
        if (LocalDate.now().isAfter(cheque.getIssueDate().plusMonths(6))) {
            throw new ChequeExpiredException("Cheque is expired and cannot be presented.");
        }
        if (drawer.getStatus() == AccountStatus.BLOCKED) {
            throw new AccountBlockedException("Drawer account is blocked.");
        }
    }
}
