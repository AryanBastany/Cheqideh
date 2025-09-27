package cheqideh.service.crud;

import cheqideh.model.account.Account;
import cheqideh.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountCrudService implements CrudService<Account, Long>{

    private final AccountRepository accountRepo;

    @Override
    public Account add(Account newAccount) {
        // TODO: validation
        return accountRepo.save(newAccount);
    }

    @Override
    public void removeById(Long accId) {
        // TODO: validation
        accountRepo.findByAccId(accId).ifPresent(accountRepo::delete);
    }

    @Override
    public Account findById(Long accId) {
        // TODO: validation
        return accountRepo.findByAccId(accId).orElse(null);
    }
}
