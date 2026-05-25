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

@Service
public class AccountService {
    private final Random random = new Random();
    private static final String IBAN_COUNTRY_CODE = "NL";
    private static final String IBAN_BANK_CODE = "INHO";

    private final AccountRepository accountRepository;
    private final AccountCreatePolicy accountCreatePolicy;

    public AccountService(AccountRepository accountRepository, AccountCreatePolicy accountCreatePolicy) {
        this.accountRepository = accountRepository;
		this.accountCreatePolicy = accountCreatePolicy;
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
        account.setIban(generateIban());

        accountCreatePolicy.enforceAccountCreatePolicy(account, issuer);

        return accountRepository.save(account);
    }

    private String generateIban() {
        // Generate 10 random account digits
        StringBuilder accountNumber = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            accountNumber.append(random.nextInt(10));
        }

        // checksum is not yet calculated
        String startingIban = IBAN_BANK_CODE + accountNumber + IBAN_COUNTRY_CODE + "00";

        // Convert letters to numbers (A=10, B=11, ..., Z=35)
        StringBuilder numeric = new StringBuilder();
        for (char c : startingIban.toCharArray()) {
            if (Character.isLetter(c)) {
                numeric.append(c - 'A' + 10);
            } else {
                numeric.append(c);
            }
        }

        // Compute mod 97
        int mod = 0;
        for (char digit : numeric.toString().toCharArray()) {
            mod = (mod * 10 + Character.getNumericValue(digit)) % 97;
        }

        int checksum = 98 - mod;

        // Format checksum as 2 digits
        String checkDigits = String.format("%02d", checksum);

        return IBAN_COUNTRY_CODE + checkDigits + IBAN_BANK_CODE + accountNumber;
    }
}
