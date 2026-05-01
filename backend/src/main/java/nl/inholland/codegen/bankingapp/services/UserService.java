package nl.inholland.codegen.bankingapp.services;

import nl.inholland.codegen.bankingapp.dtos.RegisterRequestDTO;
import nl.inholland.codegen.bankingapp.dtos.UserResponseDTO;
import nl.inholland.codegen.bankingapp.exceptions.AuthenticationException;
import nl.inholland.codegen.bankingapp.exceptions.BadRequestException;
import nl.inholland.codegen.bankingapp.models.Customer;
import nl.inholland.codegen.bankingapp.models.CustomerStatus;
import nl.inholland.codegen.bankingapp.models.Role;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.dtos.LoginRequestDTO;
import nl.inholland.codegen.bankingapp.dtos.LoginResponseDTO;
import nl.inholland.codegen.bankingapp.repositories.CustomerRepository;
import nl.inholland.codegen.bankingapp.repositories.UserRepository;
import nl.inholland.codegen.bankingapp.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository,
                       CustomerRepository customerRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {

        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new AuthenticationException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponseDTO(token, user.getRole().name());
    }

    public UserResponseDTO registerCustomer(RegisterRequestDTO request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new AuthenticationException("Email already in use");
        }

        if (userRepository.findByBsn(request.bsn()).isPresent()) {
            throw new BadRequestException("BSN already in use");
        }

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setBsn(request.bsn());
        user.setPhoneNumber(request.phoneNumber());
        user.setRole(Role.CUSTOMER);

        User savedUser = userRepository.save(user);

        Customer customer = new Customer(savedUser, CustomerStatus.PENDING);
        customerRepository.save(customer);

        return new UserResponseDTO(
            savedUser.getUserId(),
            savedUser.getFirstName(),
            savedUser.getLastName(),
            savedUser.getEmail(),
            savedUser.getRole()
        );
    }
}