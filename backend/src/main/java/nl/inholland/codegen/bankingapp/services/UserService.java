package nl.inholland.codegen.bankingapp.services;

import nl.inholland.codegen.bankingapp.dtos.*;
import nl.inholland.codegen.bankingapp.dtos.AccountCreationRequest;
import nl.inholland.codegen.bankingapp.dtos.UserPatchRequest;
import nl.inholland.codegen.bankingapp.exceptions.*;
import nl.inholland.codegen.bankingapp.exceptions.NotFoundException;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.repositories.UserRepository;
import nl.inholland.codegen.bankingapp.utils.JwtUtil;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;

@Service
public class UserService {
    private static final String INVALID_ERR_MSG = "Invalid email or password";

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

    /**
     * @return Returns the JWT token
     */
    public String login(LoginRequest request) throws AuthenticationException {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new AuthenticationException(INVALID_ERR_MSG));

        if (passwordEncoder.matches(request.password(), user.getPassword())) {
            String token = jwtUtil.generateToken(user.getEmail());
            return token;
        } else {
            throw new AuthenticationException(INVALID_ERR_MSG);
        }
    }

    public User register(User user) {
        try {
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(hashedPassword);

            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // we can optionally check what constraint is violated, but it is honestly not needed
            throw new BadRequestException("Email or BSN is already in use");
        }
    }


    public Page<User> getAllApprovedUsers(Pageable pageable) {
        return getAllUsers(true, pageable);
    }

    public Page<User> getAllUsers(Boolean isApproved, Pageable pageable) {
        // If it's null, return all users regardless of approval status
        if (isApproved == null) return userRepository.findByRole(User.Role.Customer, pageable);
        if (isApproved) {
            // If it's true return all approved users
            return userRepository.findByRoleAndApprovedByIsNotNull(User.Role.Customer, pageable);
        } else {
            // If it's false return all users waiting to be approved
            return userRepository.findByRoleAndApprovedByIsNull(User.Role.Customer, pageable);
        }
    }

    public Optional<User> getUser(long userId) {
        return userRepository.findById(userId);
    }

    public User createAccounts(AccountCreationRequest request, User approver)
            throws NotFoundException, BadRequestException {

        User user = getUser(request.userId())
            .orElseThrow(() -> new NotFoundException("User not found"));

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

    public User updateUser(UserPatchRequest request)
            throws NotFoundException, BadRequestException {
        User user = getUser(request.userId())
            .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isClosed()) {
            throw new BadRequestException("Cannot update a closed account");
        }

        //Not sure whether or not to still use JSON Patch. The code is minimal. Maybe if we add more to patch request?
        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        return userRepository.save(user);
    }

    public void deleteUser(long userId) throws NotFoundException {
        User user = getUser(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isClosed()) return;
        user.setClosed(true);
        userRepository.save(user);
    }
}
