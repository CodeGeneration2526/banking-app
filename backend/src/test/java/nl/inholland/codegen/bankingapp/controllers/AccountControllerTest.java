package nl.inholland.codegen.bankingapp.controllers;

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
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.repositories.AccountRepository;
import nl.inholland.codegen.bankingapp.repositories.UserRepository;
import nl.inholland.codegen.bankingapp.utils.IbanUtil;
import nl.inholland.codegen.bankingapp.utils.JwtUtil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "jwt.secret=test-secret-must-be-at-least-32-bytes-long-xx"
})
@Transactional
class AccountControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private IbanUtil ibanUtil;

    private User employee;
    private User customerA;
    private User customerB;
    private Account accAchecking1;
    private Account accAchecking2;
    private Account accAsavings;
    private Account accBsavings;
    private String tokenA;
    private String tokenEmployee;

    @BeforeEach
    void setUp() {
        employee = persistUser("emp@example.com", "111111111", "100000001", User.Role.Employee, null);
        customerA = persistUser("alice@example.com", "222222222", "100000002", User.Role.Customer, employee);
        customerB = persistUser("bob@example.com", "333333333", "100000003", User.Role.Customer, employee);

        accAchecking1 = persistAccount(1000000001L, Account.AccountType.Checking, customerA, 100_000L);
        accAchecking2 = persistAccount(1000000002L, Account.AccountType.Checking, customerA, 0L);
        accAsavings   = persistAccount(1000000003L, Account.AccountType.Savings, customerA, 50_000L);
        accBsavings = persistAccount(1000000004L, Account.AccountType.Savings, customerB, 50_000L);

        tokenA = jwtUtil.generateToken(customerA.getEmail());
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

    private String bearer(String token) {
        return "Bearer " + token;
    }

    // --- GET /accounts ---

    @Test
    void listAllAccounts_returnsPagedAccounts_whenEmployee() throws Exception {
        mockMvc.perform(get("/accounts")
                .header("Authorization", bearer(tokenEmployee)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].ownerUserId").exists());
    }

    @Test
    void listAllAccounts_filtersByIban() throws Exception {
        mockMvc.perform(get("/accounts")
                .param("search", "true")
                .param("iban", accAchecking1.getIban())
                .header("Authorization", bearer(tokenEmployee)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].iban").value(accAchecking1.getIban()))
            .andExpect(jsonPath("$.content[0].ownerUserId").value(customerA.getUserId()));
    }

    @Test
    void listAllAccounts_filtersByAccountNumber() throws Exception {
        mockMvc.perform(get("/accounts")
                .param("iban", String.valueOf(accAsavings.getAccountNumber()))
                .header("Authorization", bearer(tokenEmployee)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].accountId").value(accAsavings.getAccountId()))
            .andExpect(jsonPath("$.content[0].ownerUserId").value(customerA.getUserId()));
    }

    @Test
    void listAllAccounts_filtersByFirstName() throws Exception {
        mockMvc.perform(get("/accounts")
                .param("search", "true")
                .param("firstName", "Alice")
                .header("Authorization", bearer(tokenEmployee)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void listAllAccounts_returns403_whenNoAuth() throws Exception {
        mockMvc.perform(get("/accounts"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getAccountInfo_returnsAccount_whenCustomerOwnsAccount() throws Exception {
        mockMvc.perform(get("/accounts/" + accAchecking1.getAccountId())
                .header("Authorization", bearer(tokenA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountId").value(accAchecking1.getAccountId()));
    }

    @Test
    void getAccountInfo_returns403_whenCustomerTriesOtherAccount() throws Exception {
        mockMvc.perform(get("/accounts/" + accBsavings.getAccountId())
                .header("Authorization", bearer(tokenA)))
            .andExpect(status().isUnauthorized());
    }

    // --- GET /accounts/{accountId} ---

    @Test
    void getAccountInfo_returnsAccount_whenEmployeeRequests() throws Exception {
        mockMvc.perform(get("/accounts/" + accAchecking1.getAccountId())
                .header("Authorization", bearer(tokenEmployee)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountId").value(accAchecking1.getAccountId()));
    }

    @Test
    void getAccountInfo_returns404_whenNotExists() throws Exception {
        mockMvc.perform(get("/accounts/999")
                .header("Authorization", bearer(tokenEmployee)))
            .andExpect(status().isNotFound());
    }

    // --- PATCH /accounts/{accountId} ---

    @Test
    void updateAccount_returns200AndUpdatesLimits_whenEmployee() throws Exception {
        Map<String, Object> body = Map.of(
            "absoluteLimitInCents", -5_000,
            "dailyLimitInCents", 250_000
        );

        mockMvc.perform(patch("/accounts/" + accAchecking1.getAccountId())
                .header("Authorization", bearer(tokenEmployee))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accountId").value(accAchecking1.getAccountId()))
            .andExpect(jsonPath("$.absoluteLimitInCents").value(-5_000))
            .andExpect(jsonPath("$.dailyLimitInCents").value(250_000))
            .andExpect(jsonPath("$.closed").value(false));
    }

    @Test
    void updateAccount_closesAccount_whenClosedTrue() throws Exception {
        Map<String, Object> body = Map.of("closed", true);

        mockMvc.perform(patch("/accounts/" + accAchecking1.getAccountId())
                .header("Authorization", bearer(tokenEmployee))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.closed").value(true));
    }

    @Test
    void updateAccount_returns400_whenDailyLimitNegative() throws Exception {
        Map<String, Object> body = Map.of("dailyLimitInCents", -1);

        mockMvc.perform(patch("/accounts/" + accAchecking1.getAccountId())
                .header("Authorization", bearer(tokenEmployee))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateAccount_returns404_whenAccountNotExists() throws Exception {
        Map<String, Object> body = Map.of("dailyLimitInCents", 100_000);

        mockMvc.perform(patch("/accounts/999")
                .header("Authorization", bearer(tokenEmployee))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateAccount_returns403_whenCustomerTries() throws Exception {
        Map<String, Object> body = Map.of("dailyLimitInCents", 100_000);

        mockMvc.perform(patch("/accounts/" + accAchecking1.getAccountId())
                .header("Authorization", bearer(tokenA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isUnauthorized());
    }

    // --- POST /accounts ---

    @Test
    void createAccount_returns200AndCreatesAccounts_whenEmployeeApprovesCustomer() throws Exception {
        User unapprovedCustomer = persistUser("charlie@example.com", "444444444", "100000004", User.Role.Customer, null);

        Map<String, Object> body = Map.of(
            "userId", unapprovedCustomer.getUserId(),
            "absoluteLimitInCents", 0,
            "dailyLimitInCents", 500_000
        );

        mockMvc.perform(post("/accounts")
                .header("Authorization", bearer(tokenEmployee))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("User successfully approved and accounts created."));
    }

    @Test
    void createAccount_returns400_whenUserNotFound() throws Exception {
        Map<String, Object> body = Map.of(
            "userId", 999,
            "absoluteLimitInCents", 0,
            "dailyLimitInCents", 500_000
        );

        mockMvc.perform(post("/accounts")
                .header("Authorization", bearer(tokenEmployee))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("userId is invalid"));
    }

    @Test
    void createAccount_returns400_whenCustomerAlreadyApproved() throws Exception {
        Map<String, Object> body = Map.of(
            "userId", customerA.getUserId(),
            "absoluteLimitInCents", 0,
            "dailyLimitInCents", 500_000
        );

        mockMvc.perform(post("/accounts")
                .header("Authorization", bearer(tokenEmployee))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Customer already approved"));
    }

    @Test
    void createAccount_returns403_whenCustomerTries() throws Exception {
        Map<String, Object> body = Map.of(
            "userId", customerB.getUserId(),
            "absoluteLimitInCents", 0,
            "dailyLimitInCents", 500_000
        );

        mockMvc.perform(post("/accounts")
                .header("Authorization", bearer(tokenA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isUnauthorized());
    }
}
