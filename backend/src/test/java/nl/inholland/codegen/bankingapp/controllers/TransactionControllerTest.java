package nl.inholland.codegen.bankingapp.controllers;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.inholland.codegen.bankingapp.models.Account;
import nl.inholland.codegen.bankingapp.models.Transaction;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.repositories.AccountRepository;
import nl.inholland.codegen.bankingapp.repositories.TransactionRepository;
import nl.inholland.codegen.bankingapp.repositories.UserRepository;
import nl.inholland.codegen.bankingapp.utils.IbanUtil;
import nl.inholland.codegen.bankingapp.utils.JwtUtil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "jwt.secret=test-secret-must-be-at-least-32-bytes-long-xx"
})
@Transactional
class TransactionControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private IbanUtil ibanUtil;

    private User employee;
    private User customerA;
    private User customerB;
    private Account accAchecking1;
    private Account accAchecking2;
    private Account accAsavings;
    private Account accBchecking;
    private Account accBchecking2;
    private String tokenA;
    private String tokenB;
    private String tokenEmployee;

    @BeforeEach
    void setUp() {
        employee = persistUser("emp@example.com", "111111111", "100000001", User.Role.Employee, null);
        customerA = persistUser("alice@example.com", "222222222", "100000002", User.Role.Customer, employee);
        customerB = persistUser("bob@example.com",   "333333333", "100000003", User.Role.Customer, employee);

        accAchecking1 = persistAccount(1000000001L, Account.AccountType.Checking, customerA, 100_000L);
        accAchecking2 = persistAccount(1000000002L, Account.AccountType.Checking, customerA, 0L);
        accAsavings   = persistAccount(1000000003L, Account.AccountType.Savings,  customerA, 50_000L);
        accBchecking  = persistAccount(1000000004L, Account.AccountType.Checking, customerB, 100_000L);
        accBchecking2 = persistAccount(1000000005L, Account.AccountType.Checking, customerB, 0L);

        tokenA        = jwtUtil.generateToken(customerA.getEmail());
        tokenB        = jwtUtil.generateToken(customerB.getEmail());
        tokenEmployee = jwtUtil.generateToken(employee.getEmail());
    }

    private User persistUser(String email, String phone, String bsn, User.Role role, User approvedBy) {
        User u = new User();
        u.setFirstName("First");
        u.setLastName("Last");
        u.setEmail(email);
        u.setPhoneNumber(phone);
        u.setBsn(bsn);
        u.setPassword(passwordEncoder.encode("pw"));
        u.setRegistrationDate(new Date());
        u.setRole(role);
        u.setApprovedBy(approvedBy);
        return userRepository.save(u);
    }

    private Account persistAccount(long accountNumber, Account.AccountType type, User owner, long balance) {
        Account a = new Account();
        a.setAccountNumber(accountNumber);
        a.setAccountType(type);
        a.setOwner(owner);
        a.setStoredAmountInCents(balance);
        a.setAbsoluteLimitInCents(0L);
        a.setDailyLimitInCents(Account.DEFAULT_DAILY_LIMIT);
        a.setCreationDate(new Date());
        a.setClosed(false);
        if (type == Account.AccountType.Checking) {
            a.setIban(ibanUtil.generateIban(accountNumber));
        }
        return accountRepository.save(a);
    }

    private Transaction persistTransaction(Account from, Account to, long amount, User initiator) {
        Transaction t = new Transaction();
        t.setSenderAccount(from);
        t.setReceiverAccount(to);
        t.setAmountInCents(amount);
        t.setTimestamp(LocalDateTime.now());
        t.setInitiatedBy(initiator);
        return transactionRepository.save(t);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    // --- POST /transactions ---

    // End-to-end happy path: request goes through the JWT filter, validation, controller,
    // service, policy, mapper, JSON serialization. Also re-fetches accounts from the repo
    // to confirm balances actually changed (not just that the response said so).
    @Test
    void executeTransaction_returns201AndPersistsBalances_whenCustomerTransfersBetweenOwnAccounts() throws Exception {
        Map<String, Object> body = Map.of(
            "from", accAchecking1.getIban(),
            "to",   accAchecking2.getIban(),
            "amountInCents", 25_000);

        mockMvc.perform(post("/transactions")
                .header("Authorization", bearer(tokenA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.amountInCents").value(25_000))
            .andExpect(jsonPath("$.from.identifier").value(accAchecking1.getIban()))
            .andExpect(jsonPath("$.to.identifier").value(accAchecking2.getIban()))
            .andExpect(jsonPath("$.initiatedBy").value(customerA.getEmail()))
            .andExpect(jsonPath("$.transactionId").isNumber());

        Account sender   = accountRepository.findByAccountNumber(accAchecking1.getAccountNumber()).orElseThrow();
        Account receiver = accountRepository.findByAccountNumber(accAchecking2.getAccountNumber()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(75_000L, sender.getStoredAmountInCents());
        org.junit.jupiter.api.Assertions.assertEquals(25_000L, receiver.getStoredAmountInCents());
    }

    @Test
    void executeTransaction_returns400_whenAmountNotPositive() throws Exception {
        Map<String, Object> body = Map.of(
            "from", accAchecking1.getIban(),
            "to",   accAchecking2.getIban(),
            "amountInCents", 0);

        mockMvc.perform(post("/transactions")
                .header("Authorization", bearer(tokenA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void executeTransaction_returns400_whenFromBlank() throws Exception {
        Map<String, Object> body = Map.of(
            "from", "",
            "to",   accAchecking2.getIban(),
            "amountInCents", 1_000);

        mockMvc.perform(post("/transactions")
                .header("Authorization", bearer(tokenA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest());
    }

    // Proves the policy's BadRequestException is mapped to 400 with the rule's message in the
    // ApiResponse body. Also exercises the numeric-account-number branch of resolveAccountNumber
    // since savings accounts have no IBAN.
    @Test
    void executeTransaction_returns400_whenTransferViolatesPolicy_savingsToOtherOwner() throws Exception {
        Map<String, Object> body = Map.of(
            "from", String.valueOf(accAsavings.getAccountNumber()),
            "to",   accBchecking.getIban(),
            "amountInCents", 1_000);

        mockMvc.perform(post("/transactions")
                .header("Authorization", bearer(tokenA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Savings transfers must stay between your own accounts"));
    }

    // Bumps balance high enough that the absolute-limit check passes, then seeds a same-day
    // transaction so the new amount tips the daily total past the cap. Final balance assert
    // confirms no partial debit occurred when the limit fired.
    @Test
    void executeTransaction_returns400_whenDailyLimitExceeded() throws Exception {
        accAchecking1.setStoredAmountInCents(1_000_000L);
        accAchecking1.setDailyLimitInCents(500_000L);
        accountRepository.save(accAchecking1);
        persistTransaction(accAchecking1, accAchecking2, 400_000L, customerA);

        Map<String, Object> body = Map.of(
            "from", accAchecking1.getIban(),
            "to",   accAchecking2.getIban(),
            "amountInCents", 150_000);

        mockMvc.perform(post("/transactions")
                .header("Authorization", bearer(tokenA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Daily transfer limit exceeded"));

        Account sender = accountRepository.findByAccountNumber(accAchecking1.getAccountNumber()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(1_000_000L, sender.getStoredAmountInCents());
    }

    // --- GET /transactions ---

    // Seeds two transactions: one entirely on customerA's accounts, one entirely on customerB's.
    // CustomerA's scope should see only her own — confirms the owner-scoped Specification
    // actually filters at the DB layer.
    @Test
    void getTransactions_returnsOnlyOwnTransactions_forCustomer() throws Exception {
        persistTransaction(accAchecking1, accAchecking2, 5_000L, customerA);
        persistTransaction(accBchecking, accBchecking2, 1_000L, customerB);

        mockMvc.perform(get("/transactions")
                .header("Authorization", bearer(tokenA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].initiatedBy").value(customerA.getEmail()));
    }
    
    @Test
    void getTransactions_employeeWithUserIdFilter_returnsThatUsersTransactions() throws Exception {
        persistTransaction(accAchecking1, accAchecking2, 5_000L, customerA);
        persistTransaction(accBchecking, accBchecking2, 1_000L, customerB);

        mockMvc.perform(get("/transactions")
                .param("userId", String.valueOf(customerB.getUserId()))
                .header("Authorization", bearer(tokenEmployee)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].initiatedBy").value(customerB.getEmail()));
    }

    @Test
    void getTransactions_returns403_whenNoAuthHeader() throws Exception {
        mockMvc.perform(get("/transactions"))
            .andExpect(status().is(org.springframework.http.HttpStatus.FORBIDDEN.value()));
    }
}
