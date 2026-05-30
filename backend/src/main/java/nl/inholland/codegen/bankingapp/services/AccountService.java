package nl.inholland.codegen.bankingapp.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nl.inholland.codegen.bankingapp.exceptions.NotFoundException;
import nl.inholland.codegen.bankingapp.models.*;
import nl.inholland.codegen.bankingapp.policies.AccountCreatePolicy;
import nl.inholland.codegen.bankingapp.policies.ApproveUsersPolicy;
import nl.inholland.codegen.bankingapp.repositories.AccountRepository;
import nl.inholland.codegen.bankingapp.repositories.UserRepository;
import nl.inholland.codegen.bankingapp.utils.IbanUtil;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountCreatePolicy accountCreatePolicy;
    private final ApproveUsersPolicy approveUsersPolicy;
    private final IbanUtil ibanUtil;

    public AccountService(
            AccountRepository accountRepository,
            UserRepository userRepository,
            AccountCreatePolicy accountCreatePolicy,
            ApproveUsersPolicy approveUsersPolicy,
            IbanUtil ibanUtil
    ) {
        this.accountRepository = accountRepository;
		this.userRepository = userRepository;
		this.accountCreatePolicy = accountCreatePolicy;
		this.approveUsersPolicy = approveUsersPolicy;
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

    @Transactional
    public void approveAndCreateAccounts(
            User user,
            User issuer,
            long absoluteLimitInCents,
            long dailyLimitInCents
    ) {
        approveUsersPolicy.enforceApproveUsersPolicy(user, issuer);

        user.setApprovedBy(issuer);
        userRepository.save(user);

        Account checking = Account.builder()
            .owner(user)
            .accountType(Account.AccountType.Checking)
            .absoluteLimitInCents(absoluteLimitInCents)
            .dailyLimitInCents(dailyLimitInCents)
            .build();

        Account savings = Account.builder()
            .owner(user)
            .accountType(Account.AccountType.Savings)
            .absoluteLimitInCents(absoluteLimitInCents)
            .dailyLimitInCents(dailyLimitInCents)
            .build();

        createAccount(checking, issuer);
        createAccount(savings, issuer);
    }
}
