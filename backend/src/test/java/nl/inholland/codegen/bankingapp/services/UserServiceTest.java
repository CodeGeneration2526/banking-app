package nl.inholland.codegen.bankingapp.services;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import nl.inholland.codegen.bankingapp.dtos.LoginRequest;
import nl.inholland.codegen.bankingapp.dtos.UserPatchRequest;
import nl.inholland.codegen.bankingapp.exceptions.AuthenticationException;
import nl.inholland.codegen.bankingapp.exceptions.BadRequestException;
import nl.inholland.codegen.bankingapp.exceptions.NotFoundException;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.repositories.UserRepository;
import nl.inholland.codegen.bankingapp.utils.JwtUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    private User customer;
    private User employee;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setUserId(1L);
        customer.setEmail("customer@example.com");
        customer.setPassword("hashedPassword");
        customer.setRole(User.Role.Customer);
        customer.setClosed(false);

        employee = new User();
        employee.setUserId(99L);
        employee.setEmail("employee@example.com");
        employee.setPassword("hashedEmpPassword");
        employee.setRole(User.Role.Employee);
        employee.setClosed(false);
    }

    @Test
    void login_returnsToken_whenCredentialsValid() {
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("customer@example.com")).thenReturn("jwt-token-123");

        String token = userService.login(new LoginRequest("customer@example.com", "password"));

        assertEquals("jwt-token-123", token);
        verify(jwtUtil).generateToken("customer@example.com");
    }

    @Test
    void login_throwsAuthenticationException_whenUserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        AuthenticationException ex = assertThrows(AuthenticationException.class,
            () -> userService.login(new LoginRequest("nonexistent@example.com", "password")));
        assertEquals("Invalid email or password", ex.getMessage());
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void login_throwsAuthenticationException_whenPasswordMismatch() {
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

        AuthenticationException ex = assertThrows(AuthenticationException.class,
            () -> userService.login(new LoginRequest("customer@example.com", "wrongpassword")));
        assertEquals("Invalid email or password", ex.getMessage());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void register_encodesPasswordAndSavesUser() {
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setUserId(10L);
            return u;
        });

        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setPassword("plainPassword");

        User result = userService.register(newUser);

        assertEquals("encodedPassword", result.getPassword());
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(newUser);
    }

    @Test
    void register_throwsBadRequestException_whenEmailOrBsnAlreadyExists() {
        when(userRepository.save(any(User.class))).thenThrow(DataIntegrityViolationException.class);

        User newUser = new User();
        newUser.setEmail("existing@example.com");
        newUser.setPassword("plainPassword");

        assertThrows(BadRequestException.class, () -> userService.register(newUser));
    }

    @Test
    void getAllUsers_withRole_returnsPageOfUsersByRole() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(java.util.List.of(customer));
        when(userRepository.findByRole(User.Role.Customer, pageable)).thenReturn(page);

        Page<User> result = userService.getAllUsers(null, User.Role.Customer, pageable);

        assertSame(page, result);
        verify(userRepository).findByRole(User.Role.Customer, pageable);
    }

    @Test
    void getAllUsers_withIsApprovedTrueAndNoRole_returnsApprovedUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(java.util.List.of(customer));
        customer.setApprovedBy(employee);
        when(userRepository.findByApprovedByIsNotNull(pageable)).thenReturn(page);

        Page<User> result = userService.getAllUsers(true, pageable);

        assertSame(page, result);
        verify(userRepository).findByApprovedByIsNotNull(pageable);
    }

    @Test
    void getAllUsers_withIsApprovedFalseAndNoRole_returnsUnapprovedUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(java.util.List.of(customer));
        when(userRepository.findByApprovedByIsNull(pageable)).thenReturn(page);

        Page<User> result = userService.getAllUsers(false, pageable);

        assertSame(page, result);
        verify(userRepository).findByApprovedByIsNull(pageable);
    }

    @Test
    void getAllUsers_withIsApprovedNullAndNoRole_returnsAllUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(java.util.List.of(customer, employee));
        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<User> result = userService.getAllUsers(null, pageable);

        assertSame(page, result);
        verify(userRepository).findAll(pageable);
    }

    @Test
    void getUser_returnsUser_whenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));

        Optional<User> result = userService.getUser(1L);

        assertTrue(result.isPresent());
        assertSame(customer, result.get());
    }

    @Test
    void getUser_returnsEmptyOptional_whenNotExists() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUser(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void updateUser_updatesFieldsAndSaves() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserPatchRequest request = new UserPatchRequest("NewFirst", "NewLast", "newemail@example.com", true);
        User result = userService.updateUser(1L, request);

        assertEquals("NewFirst", result.getFirstName());
        assertEquals("NewLast", result.getLastName());
        assertEquals("newemail@example.com", result.getEmail());
        assertTrue(result.isClosed());
    }

    @Test
    void updateUser_throwsNotFoundException_whenUserNotExists() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserPatchRequest request = new UserPatchRequest("NewFirst", null, null, null);

        assertThrows(NotFoundException.class, () -> userService.updateUser(999L, request));
    }

    @Test
    void updateUser_throwsBadRequestException_whenUserIsClosed() {
        customer.setClosed(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));

        UserPatchRequest request = new UserPatchRequest("NewFirst", null, null, null);

        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> userService.updateUser(1L, request));
        assertEquals("Cannot update a closed account", ex.getMessage());
    }

    @Test
    void updateUser_throwsBadRequestException_whenEmailAlreadyInUse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(userRepository.save(any(User.class))).thenThrow(DataIntegrityViolationException.class);

        UserPatchRequest request = new UserPatchRequest(null, null, "existing@example.com", null);

        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> userService.updateUser(1L, request));
        assertEquals("Email is already in use", ex.getMessage());
    }
}

