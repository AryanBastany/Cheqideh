package Cheqideh.service.crud;

import Cheqideh.model.account.Account;
import Cheqideh.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountCrudService implements CrudService<Account, Long>{

    private final AccountRepository accountRepo;

    @Override
    public void add(Account newAccount) {
        // TODO: validation
        accountRepo.save(newAccount);
    }

    @Override
    public void removeById(Long id) {
        // TODO: validation
        accountRepo.findById(id).ifPresent(accountRepo::delete);
    }

    @Override
    public Account findById(Long id) {
        // TODO: validation
        return accountRepo.findById(id).orElse(null);
    }
}
