package nl.inholland.codegen.bankingapp;

import nl.inholland.codegen.bankingapp.models.Account;
import nl.inholland.codegen.bankingapp.models.Transaction;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.repositories.AccountRepository;
import nl.inholland.codegen.bankingapp.repositories.TransactionRepository;
import nl.inholland.codegen.bankingapp.repositories.UserRepository;
import nl.inholland.codegen.bankingapp.services.AccountService;
import nl.inholland.codegen.bankingapp.services.TransactionService;
import nl.inholland.codegen.bankingapp.services.UserService;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds demo data by going through the same services the API uses, so the seeded
 * state is always consistent with the real approval / transfer rules.
 *
 * Logins (password "Customer1!" for customers, "Employee1!/Employee2!" for staff):
 *   - Employees:           john.admin@bank.com, sarah.smith@bank.com
 *   - Known customers:     alice.johnson@email.com, bob.williams@email.com (from the README)
 *   - Approved customers:  approved1@email.com … approved23@email.com (random names)
 *   - Pending customers:   pending1@email.com  … pending25@email.com  (random names)
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final int COUNT = 25;
    private static final String CUSTOMER_PASSWORD = "Customer1!";

    private static final String[] NAMES = {
        "Alice", "Bob", "Charlie", "Diana", "Ethan", "Fiona", "George", "Hannah",
        "Ivan", "Julia", "Kevin", "Laura", "Mason", "Nina", "Oscar"
    };

    private final java.util.Random random = new java.util.Random();

    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public DataSeeder(UserService userService, AccountService accountService,
                      TransactionService transactionService, UserRepository userRepository,
                      AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.userService = userService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        User emp1 = createEmployee("John", "Admin", "john.admin@bank.com", "Employee1!", 1);
        // Self-approval: save again with approvedBy set (registrationDate is updatable=false, unaffected)
        emp1.setApprovedBy(emp1);
        emp1 = userRepository.save(emp1);
        createEmployee("Sarah", "Smith", "sarah.smith@bank.com", "Employee2!", 2);

        // 25 pending customers: registered but never approved, so they have no accounts.
        for (int i = 1; i <= COUNT; i++) {
            String firstName = randomName();
            String lastName = randomName();
            registerCustomer(firstName, lastName, i + firstName + "." + lastName + "@email.com", 100 + i);
        }

        // Approved customers (approved via the service, which also creates their accounts).
        List<Account> checkingAccounts = new ArrayList<>();

        // Known logins from the README, hardcoded for consistent manual testing.
        approveCustomer("Alice", "Johnson", "alice.johnson@email.com", 300, emp1, checkingAccounts);
        approveCustomer("Bob", "Williams", "bob.williams@email.com", 301, emp1, checkingAccounts);

        // The rest get random names; total stays at COUNT.
        for (int i = 1; i <= COUNT; i++) {
            String firstName = randomName();
            String lastName = randomName();
            approveCustomer(firstName, lastName, firstName + "." + lastName + i + "@email.com", 200 + i, emp1, checkingAccounts);
        }

        seedTransactions(checkingAccounts, emp1);
    }

    /** Registers a customer, approves them through the service, and funds their accounts. */
    private void approveCustomer(String firstName, String lastName, String email, int n,
                                 User issuer, List<Account> checkingAccounts) {
        User customer = registerCustomer(firstName, lastName, email, n);
        accountService.approveAndCreateAccounts(customer, issuer, Account.DEFAULT_ABSOLUTE_LIMIT, Account.DEFAULT_DAILY_LIMIT);

        // Give each account an opening balance (there is no deposit endpoint to go through).
        for (Account account : accountRepository.findByOwner(customer)) {
            long opening = account.getAccountType() == Account.AccountType.Checking ? 500_000L : 1_000_000L;
            account.setStoredAmountInCents(opening);
            accountRepository.save(account);
            if (account.getAccountType() == Account.AccountType.Checking) {
                checkingAccounts.add(account);
            }
        }
    }

    /**
     * Runs employee-initiated checking-to-checking transfers through the real
     * TransactionService (so balances and limits are honoured), then back-dates
     * each one to spread the history over the last 30 days.
     */
    private void seedTransactions(List<Account> checking, User initiator) {
        LocalDateTime now = LocalDateTime.now();
        int day = 0;

        // Two passes so each customer both sends and receives a couple of transfers.
        for (int round = 0; round < 2; round++) {
            for (int i = 0; i < checking.size(); i++) {
                Account sender = checking.get(i);
                Account receiver = checking.get((i + 1 + round * 6) % checking.size());
                long amountInCents = 1_000L + (i % 10) * 1_500L + round * 2_000L;

                Transaction t = transactionService.executeTransaction(
                    sender.getAccountNumber(), receiver.getAccountNumber(), amountInCents, initiator);

                // executeTransaction stamps "now"; back-date for a realistic spread.
                t.setTimestamp(now.minusDays(day % 30).minusHours(i));
                transactionRepository.save(t);
                day++;
            }
        }
    }

    private User createEmployee(String firstName, String lastName, String email, String password, int n) {
        User employee = User.builder()
            .firstName(firstName).lastName(lastName)
            .email(email).phoneNumber(phone(n)).bsn(bsn(n))
            .password(password).role(User.Role.Employee)
            .build();
        return userService.register(employee); // hashes the password and persists
    }

    private User registerCustomer(String firstName, String lastName, String email, int n) {
        User customer = User.builder()
            .firstName(firstName).lastName(lastName)
            .email(email).phoneNumber(phone(n)).bsn(bsn(n))
            .password(CUSTOMER_PASSWORD)
            .build(); // role defaults to Customer, approvedBy stays null
        return userService.register(customer);
    }

    private String randomName() {
        return NAMES[random.nextInt(NAMES.length)];
    }

    private String phone(int n) {
        return String.format("+3160%07d", n);
    }

    private String bsn(int n) {
        return String.format("%09d", n);
    }
}
