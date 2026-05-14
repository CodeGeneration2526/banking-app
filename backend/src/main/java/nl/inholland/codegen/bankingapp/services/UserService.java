package nl.inholland.codegen.bankingapp.services;

import nl.inholland.codegen.bankingapp.dtos.LoginRequest;
import nl.inholland.codegen.bankingapp.dtos.LoginResponse;
import nl.inholland.codegen.bankingapp.dtos.RegisterRequest;
import nl.inholland.codegen.bankingapp.dtos.UserResponse;
import nl.inholland.codegen.bankingapp.exceptions.AuthenticationException;
import nl.inholland.codegen.bankingapp.exceptions.BadRequestException;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
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
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BadRequestException("Email is already in use");
        }
        if (userRepository.findByBsn(request.bsn()).isPresent()) {
            throw new BadRequestException("BSN is already in use");
        }

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setBsn(request.bsn());
        user.setPhoneNumber(request.phoneNumber());
        user.setRole(User.Role.CUSTOMER);

        User savedUser = userRepository.save(user);

        // Customer customer = new Customer();
        // customer.setUser(savedUser);
        // customer.setStatus(User.CustomerStatus.PENDING);
        // customerRepository.save(customer);

        return new UserResponse(
            savedUser.getUserId(),
            savedUser.getFirstName(),
            savedUser.getLastName(),
            savedUser.getEmail(),
            savedUser.getRole()
        );
    }
}
