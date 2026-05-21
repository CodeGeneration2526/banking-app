package nl.inholland.codegen.bankingapp.services;

import nl.inholland.codegen.bankingapp.repositories.UserRepository;
import nl.inholland.codegen.bankingapp.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @Test
    void login_WithValidCredentials_ReturnsToken() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void login_WithInvalidCredentials_ThrowsAuthenticationException() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void register_WithValidUser_ReturnsCreatedUser() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void register_WithDuplicateEmail_ThrowsBadRequestException() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void getAllApprovedUsers_ReturnsPageOfApprovedUsers() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void getUser_WithValidId_ReturnsUser() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void getUser_WithInvalidId_ReturnsEmpty() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void createAccounts_WithValidRequest_ReturnsUserWithAccounts() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void createAccounts_WithInvalidUser_ThrowsNotFoundException() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void updateUser_WithValidRequest_ReturnsUpdatedUser() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void updateUser_WithInvalidId_ThrowsNotFoundException() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void deleteUser_WithValidId_DeletesUser() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void deleteUser_WithInvalidId_ThrowsNotFoundException() {
        throw new IllegalStateException("Unimplemented");
    }
}
