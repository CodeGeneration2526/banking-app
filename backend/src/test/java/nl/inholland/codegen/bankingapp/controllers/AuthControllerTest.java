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
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Test
    void login_WithValidCredentials_ReturnsOkWithToken() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void login_WithInvalidCredentials_ReturnsUnauthorized() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void register_WithValidRequest_ReturnsCreated() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void register_WithDuplicateEmail_ReturnsBadRequest() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }
}
