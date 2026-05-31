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

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "jwt.secret=test-secret-must-be-at-least-32-bytes-long-xx"
})
@Transactional
class UserControllerTest {

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
    private String tokenEmployee;
    private String tokenCustomerA;

    @BeforeEach
    void setUp() {
        employee = persistUser("emp@example.com", "111111111", "100000001", User.Role.Employee, null);
        customerA = persistUser("alice@example.com", "222222222", "100000002", User.Role.Customer, employee);
        customerB = persistUser("bob@example.com", "333333333", "100000003", User.Role.Customer, null);

        tokenEmployee = jwtUtil.generateToken(employee.getEmail());
        tokenCustomerA = jwtUtil.generateToken(customerA.getEmail());
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

    private String bearer(String token) {
        return "Bearer " + token;
    }

    // --- GET /users ---

    @Test
    void getAllUsers_returnsPagedUsers_whenEmployee() throws Exception {
        mockMvc.perform(get("/users")
                .header("Authorization", bearer(tokenEmployee)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAllUsers_returns401_whenCustomer() throws Exception {
        mockMvc.perform(get("/users")
                .header("Authorization", bearer(tokenCustomerA)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllUsers_returns403_whenNoAuth() throws Exception {
        mockMvc.perform(get("/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_withIsApprovedFilter_returnsOnlyUnapprovedUsers() throws Exception {
        mockMvc.perform(get("/users")
                .param("isApproved", "false")
                .header("Authorization", bearer(tokenEmployee)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAllUsers_withRoleFilter_returnsOnlyCustomers() throws Exception {
        mockMvc.perform(get("/users")
                .param("role", "Customer")
                .header("Authorization", bearer(tokenEmployee)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    // --- GET /users/me ---

    @Test
    void getSelfUser_returnsCurrentUser() throws Exception {
        mockMvc.perform(get("/users/me")
                .header("Authorization", bearer(tokenCustomerA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void getSelfUser_returns401_whenNoAuth() throws Exception {
        mockMvc.perform(get("/users/me"))
            .andExpect(status().isForbidden());
    }

    // --- GET /users/{userId} ---

    @Test
    void getUser_returnsUser_whenExists() throws Exception {
        mockMvc.perform(get("/users/" + customerA.getUserId())
                .header("Authorization", bearer(tokenCustomerA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void getUser_returns404_whenNotExists() throws Exception {
        mockMvc.perform(get("/users/999")
                .header("Authorization", bearer(tokenEmployee)))
            .andExpect(status().isNotFound());
    }

    // --- PATCH /users/{userId} ---

    @Test
    void updateUser_updatesFields_whenCustomerUpdatesSelf() throws Exception {
        Map<String, String> body = Map.of(
            "firstName", "AliceUpdated",
            "lastName", "LastUpdated"
        );

        mockMvc.perform(patch("/users/" + customerA.getUserId())
                .header("Authorization", bearer(tokenCustomerA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("AliceUpdated"))
            .andExpect(jsonPath("$.lastName").value("LastUpdated"));
    }

    @Test
    void updateUser_updatesFields_whenEmployeeUpdatesOther() throws Exception {
        Map<String, String> body = Map.of(
            "firstName", "BobUpdated"
        );

        mockMvc.perform(patch("/users/" + customerB.getUserId())
                .header("Authorization", bearer(tokenEmployee))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("BobUpdated"));
    }

    @Test
    void updateUser_returns401_whenCustomerUpdatesOther() throws Exception {
        Map<String, String> body = Map.of(
            "firstName", "BobHacked"
        );

        mockMvc.perform(patch("/users/" + customerB.getUserId())
                .header("Authorization", bearer(tokenCustomerA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void updateUser_returns404_whenUserNotExists() throws Exception {
        Map<String, String> body = Map.of(
            "firstName", "Hacker"
        );

        mockMvc.perform(patch("/users/999")
                .header("Authorization", bearer(tokenEmployee))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isNotFound());
    }

    // --- DELETE /users/{userId} ---

    @Test
    void deleteUser_returns200_whenEmployeeDeletes() throws Exception {
        mockMvc.perform(delete("/users/" + customerB.getUserId())
                .header("Authorization", bearer(tokenEmployee)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("User with the id " + customerB.getUserId() + " has been closed"));
    }

    @Test
    void deleteUser_returns401_whenCustomerTries() throws Exception {
        mockMvc.perform(delete("/users/" + customerB.getUserId())
                .header("Authorization", bearer(tokenCustomerA)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteUser_returns404_whenUserNotExists() throws Exception {
        mockMvc.perform(delete("/users/999")
                .header("Authorization", bearer(tokenEmployee)))
            .andExpect(status().isNotFound());
    }
}