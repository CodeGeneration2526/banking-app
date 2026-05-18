package nl.inholland.codegen.bankingapp.services;

import nl.inholland.codegen.bankingapp.dtos.AccountCreationRequest;
import nl.inholland.codegen.bankingapp.dtos.LoginRequest;
import nl.inholland.codegen.bankingapp.dtos.LoginResponse;
import nl.inholland.codegen.bankingapp.dtos.RegisterRequest;
import nl.inholland.codegen.bankingapp.dtos.UserPatchRequest;
import nl.inholland.codegen.bankingapp.exceptions.AuthenticationException;
import nl.inholland.codegen.bankingapp.exceptions.BadRequestException;
import nl.inholland.codegen.bankingapp.exceptions.NotFoundException;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.repositories.UserRepository;
import nl.inholland.codegen.bankingapp.utils.JwtUtil;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        return new LoginResponse("TODO", user.getRole().name());
    }

    public User registerCustomer(RegisterRequest request) {
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
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // we can optionally check what constraint is violated, but it is honestly not needed
            throw new BadRequestException("Email or BSN is already in use");
        }

    }

    public Page<User> getAllUsers(int page, int pageSize, Boolean hasAccount) {
        Pageable pageable = PageRequest.of(page, pageSize);

            // If it's null, return all users regardless of approval status
        if (hasAccount == null) return userRepository.findByRole(User.Role.Customer, pageable);
        if (hasAccount) {
            // If it's true return all approved users
            return userRepository.findByRoleAndApprovedByIsNotNull(User.Role.Customer, pageable);
        } else {
            // If it's false return all users waiting to be approved
            return userRepository.findByRoleAndApprovedByIsNull(User.Role.Customer, pageable);
        }
    }

    public User getUser(long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User createAccounts(AccountCreationRequest request, User approver) {
        User user = getUser(request.userId());

        if (user.isClosed()) {
            throw new BadRequestException("Cannot approve a closed account");
        }
        if (user.getApprovedBy() != null) {
            throw new BadRequestException("Customer already approved");
        }

        user.setApprovedBy(approver);
        // TODO: create checking + savings accounts after transaction stuff is implemented
        return userRepository.save(user);
    }

    public User updateUser(UserPatchRequest request) {
        User user = getUser(request.userId());

        if (user.isClosed()) {
            throw new BadRequestException("Cannot update a closed account");
        }

        //Not sure whether or not to still use JSON Patch. The code is minimal. Maybe if we add more to patch request?
        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        return userRepository.save(user);
    }

    public void deleteUser(long userId) {
        User user = getUser(userId);

        if (user.isClosed()) return;
        user.setClosed(true);
        userRepository.save(user);
    }
}
