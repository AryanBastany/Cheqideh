package cheqideh.service.business;

import cheqideh.client.SayadClient;
import cheqideh.dto.request.AddAccountRequest;
import cheqideh.dto.request.IssueChequeRequest;
import cheqideh.exception.servicelayer.ChequeBounceException;
import cheqideh.model.BounceRecord;
import cheqideh.model.account.Account;
import cheqideh.model.account.AccountStatus;
import cheqideh.model.cheque.Cheque;
import cheqideh.model.cheque.ChequeStatus;
import cheqideh.service.crud.AccountCrudService;
import cheqideh.service.crud.BounceRecCrudService;
import cheqideh.service.crud.ChequeCrudService;
import cheqideh.validator.servicelayer.ChequeServiceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountCrudService accountCrudService;

    @Transactional
    public Account addAccount(AddAccountRequest request) throws AccountNotFoundException {

        Account newAccount = new Account();
        newAccount.setAccId(request.getAccId());
        newAccount.setBalance(request.getBalance());
        newAccount.setStatus(AccountStatus.ACTIVE);

        return accountCrudService.add(newAccount);
    }
}
