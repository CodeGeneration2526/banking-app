package nl.inholland.codegen.bankingapp;

import nl.inholland.codegen.bankingapp.models.Account;
import nl.inholland.codegen.bankingapp.models.Transaction;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.repositories.AccountRepository;
import nl.inholland.codegen.bankingapp.repositories.TransactionRepository;
import nl.inholland.codegen.bankingapp.repositories.UserRepository;
import nl.inholland.codegen.bankingapp.utils.IbanUtil;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final IbanUtil ibanUtil;

    public DataSeeder(UserRepository userRepository, AccountRepository accountRepository,
                      TransactionRepository transactionRepository, PasswordEncoder passwordEncoder,
                      IbanUtil ibanUtil) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
		this.ibanUtil = ibanUtil;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Employees
        User emp1 = userRepository.save(User.builder()
                .firstName("John").lastName("Admin")
                .email("john.admin@bank.com").phoneNumber("+31611111111")
                .bsn("111111111").password(passwordEncoder.encode("Employee1!"))
                .role(User.Role.Employee).build());
        // Self-approval: save again with approvedBy set (registrationDate is updatable=false, unaffected)
        emp1.setApprovedBy(emp1);
        emp1 = userRepository.save(emp1);

        User emp2 = userRepository.save(User.builder()
                .firstName("Sarah").lastName("Smith")
                .email("sarah.smith@bank.com").phoneNumber("+31622222222")
                .bsn("222222222").password(passwordEncoder.encode("Employee2!"))
                .role(User.Role.Employee).approvedBy(emp1).build());

        // Customers
        User alice = userRepository.save(User.builder()
                .firstName("Alice").lastName("Johnson")
                .email("alice.johnson@email.com").phoneNumber("+31633333333")
                .bsn("333333333").password(passwordEncoder.encode("Customer1!"))
                .approvedBy(emp1).build());

        User bob = userRepository.save(User.builder()
                .firstName("Bob").lastName("Williams")
                .email("bob.williams@email.com").phoneNumber("+31644444444")
                .bsn("444444444").password(passwordEncoder.encode("Customer1!"))
                .approvedBy(emp1).build());

        User charlie = userRepository.save(User.builder()
                .firstName("Charlie").lastName("Brown")
                .email("charlie.brown@email.com").phoneNumber("+31655555555")
                .bsn("555555555").password(passwordEncoder.encode("Customer1!"))
                .approvedBy(emp2).build());

        // Unapproved customers (no accounts, approvedBy left null)
        userRepository.save(User.builder()
                .firstName("Diana").lastName("Prince")
                .email("diana.prince@email.com").phoneNumber("+31666666666")
                .bsn("666666666").password(passwordEncoder.encode("Customer1!")).build());

        userRepository.save(User.builder()
                .firstName("Ethan").lastName("Hunt")
                .email("ethan.hunt@email.com").phoneNumber("+31677777777")
                .bsn("777777777").password(passwordEncoder.encode("Customer1!")).build());

        userRepository.save(User.builder()
                .firstName("Fiona").lastName("Green")
                .email("fiona.green@email.com").phoneNumber("+31688888888")
                .bsn("888888888").password(passwordEncoder.encode("Customer1!")).build());

        // Accounts — balances and limits in cents
        //   dailyLimit: €1,000  absoluteLimit: €-100
        Account aliceChecking  = saveAccount(alice,   Account.AccountType.Checking, "NL91INGB0001000001", 150000L,  100000L, -10000L);
        Account aliceSavings   = saveAccount(alice,   Account.AccountType.Savings,  "NL91INGB0001000002", 250000L,  100000L,      0L);
        Account bobChecking    = saveAccount(bob,     Account.AccountType.Checking, "NL91INGB0001000003",  75000L,  100000L, -10000L);
        Account bobSavings     = saveAccount(bob,     Account.AccountType.Savings,  "NL91INGB0001000004", 320000L,  100000L,      0L);
        Account charlieChecking = saveAccount(charlie, Account.AccountType.Checking, "NL91INGB0001000005", 430000L, 100000L, -10000L);
        Account charlieSavings  = saveAccount(charlie, Account.AccountType.Savings,  "NL91INGB0001000006", 120000L, 100000L,      0L);

        // Transactions spread across last 30 days, initiated by emp1
        List<Account> accts = List.of(
                aliceChecking, aliceSavings,
                bobChecking, bobSavings,
                charlieChecking, charlieSavings);

        // {senderIdx, receiverIdx, amountInCents, daysAgo}
        long[][] txns = {
                {0, 2,  5000, 29},   // Alice checking → Bob checking      €50.00
                {2, 4,  2500, 26},   // Bob checking   → Charlie checking   €25.00
                {4, 0, 10000, 23},   // Charlie checking → Alice checking  €100.00
                {1, 3, 15000, 20},   // Alice savings  → Bob savings       €150.00
                {0, 4,  3000, 18},   // Alice checking → Charlie checking   €30.00
                {2, 0,  7500, 15},   // Bob checking   → Alice checking     €75.00
                {4, 2, 20000, 12},   // Charlie checking → Bob checking    €200.00
                {3, 1,  5000,  9},   // Bob savings    → Alice savings      €50.00
                {0, 2,  1200,  6},   // Alice checking → Bob checking       €12.00
                {5, 1,  8000,  3},   // Charlie savings → Alice savings     €80.00
                {0, 4,  4500,  2},   // Alice checking → Charlie checking   €45.00
                {2, 0, 13000,  2},   // Bob checking   → Alice checking    €130.00
                {4, 2,  6000,  1},   // Charlie checking → Bob checking     €60.00
                {0, 2,  9900,  1},   // Alice checking → Bob checking       €99.00
                {2, 4, 11500,  0},   // Bob checking   → Charlie checking  €115.00
        };

        LocalDateTime now = LocalDateTime.now();
        for (long[] tx : txns) {
            Transaction t = new Transaction();
            t.setSenderAccount(accts.get((int) tx[0]));
            t.setReceiverAccount(accts.get((int) tx[1]));
            t.setAmountInCents(tx[2]);
            t.setTimestamp(now.minusDays(tx[3]));
            t.setInitiatedBy(emp1);
            transactionRepository.save(t);
        }
    }

    private Account saveAccount(User owner, Account.AccountType type, String iban, long balanceCents,
                                 long dailyLimitCents, long absoluteLimitCents) {
        long accountNumber = ibanUtil.newAccountNumber();

        Account a = new Account();
        a.setOwner(owner);
        a.setAccountType(type);
        a.setIban(iban);
        a.setAccountNumber(accountNumber);
        a.setStoredAmountInCents(balanceCents);
        a.setDailyLimitInCents(dailyLimitCents);
        a.setAbsoluteLimitInCents(absoluteLimitCents);
        // a.setCreationDate(LocalDateTime.now());
        a.setClosed(false);

        if (type == Account.AccountType.Checking) {
            a.setIban(ibanUtil.generateIban(accountNumber));
        }

        return accountRepository.save(a);
    }
}
