package nl.inholland.codegen.bankingapp.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import nl.inholland.codegen.bankingapp.repositories.UserRepository;
import nl.inholland.codegen.bankingapp.services.UserService;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Test
    void getAllUsers_AsEmployee_ReturnsPagedUsers() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void getAllUsers_AsCustomer_ReturnsForbidden() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void getUser_WithValidId_ReturnsUser() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void getUser_WithInvalidId_ReturnsNotFound() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void getSelfUser_ReturnsCurrentUser() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void createAccounts_ReturnsNotImplemented() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void updateUser_ReturnsNotImplemented() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void deleteUser_ReturnsNotImplemented() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }
}
