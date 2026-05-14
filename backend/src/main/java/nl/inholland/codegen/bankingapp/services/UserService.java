package nl.inholland.codegen.bankingapp.services;

import nl.inholland.codegen.bankingapp.dtos.LoginRequest;
import nl.inholland.codegen.bankingapp.dtos.LoginResponse;
import nl.inholland.codegen.bankingapp.dtos.RegisterRequest;
import nl.inholland.codegen.bankingapp.dtos.UserResponse;
import nl.inholland.codegen.bankingapp.exceptions.AuthenticationException;
import nl.inholland.codegen.bankingapp.exceptions.BadRequestException;
import nl.inholland.codegen.bankingapp.mappers.UserMapper;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.repositories.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        return new LoginResponse("TODO", user.getRole().name());
    }

    public UserResponse registerCustomer(RegisterRequest request) {
        User user = User.builder()
            .firstName(request.firstName())
            .lastName(request.lastName())
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .bsn(request.bsn())
            .phoneNumber(request.phoneNumber())
            .role(User.Role.Customer)
            .build();

        try {
            User savedUser = userRepository.save(user);
            return userMapper.toUserResponse(savedUser);
        } catch (DataIntegrityViolationException e) {
            // we can optionally check what constraint is violated, but it is honestly not needed
            throw new BadRequestException("Email or BSN is already in use");
        }

    }
}
