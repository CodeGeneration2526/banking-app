package nl.inholland.codegen.bankingapp.services;

import java.util.Optional;
import java.util.Random;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import nl.inholland.codegen.bankingapp.exceptions.NotFoundException;
import nl.inholland.codegen.bankingapp.models.*;
import nl.inholland.codegen.bankingapp.policies.AccountCreatePolicy;
import nl.inholland.codegen.bankingapp.repositories.AccountRepository;
import nl.inholland.codegen.bankingapp.utils.IbanUtil;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountCreatePolicy accountCreatePolicy;
    private final IbanUtil ibanUtil;

    public AccountService(
            AccountRepository accountRepository,
            AccountCreatePolicy accountCreatePolicy,
            IbanUtil ibanUtil
    ) {
        this.accountRepository = accountRepository;
		this.accountCreatePolicy = accountCreatePolicy;
		this.ibanUtil = ibanUtil;
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

    public Optional<Account> getAccountInfo(long accountId) {
        return accountRepository.findByAccountId(accountId);
    }

    public void closeAccount(long accountId) {
        Account account = accountRepository.findByAccountId(accountId)
            .orElseThrow(() -> new NotFoundException("Account with the given account ID could not be found."));

        account.setClosed(true);
        accountRepository.save(account);
    }

    public Account createAccount(Account account, User issuer) {
        Long newAccountNumber = ibanUtil.newAccountNumber();
        account.setAccountNumber(newAccountNumber);

        if (account.getAccountType() == Account.AccountType.Checking) {
            String iban = ibanUtil.generateIban(newAccountNumber);
            account.setIban(iban);
        }

        accountCreatePolicy.enforceAccountCreatePolicy(account, issuer);
        return accountRepository.save(account);
    }
}
