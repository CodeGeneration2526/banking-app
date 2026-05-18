package nl.inholland.codegen.bankingapp.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import nl.inholland.codegen.bankingapp.models.Account;
import nl.inholland.codegen.bankingapp.repositories.AccountRepository;

@Service
public class AccountService {

    AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Page<Account> searchCheckingAccounts(
            String firstName,
            String lastName,
            String iban,
            Pageable pageable
    ) {
        Account.AccountType accountType = Account.AccountType.Checking;
        return accountRepository.search(firstName, lastName, iban, accountType, pageable);
    }
}
