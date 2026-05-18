package nl.inholland.codegen.bankingapp.services;

import nl.inholland.codegen.bankingapp.dtos.*;
import nl.inholland.codegen.bankingapp.exceptions.*;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.repositories.UserRepository;
import nl.inholland.codegen.bankingapp.utils.JwtUtil;

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

        if (passwordEncoder.matches(request.password(), request.password())) {
            String token = jwtUtil.generateToken(user.getEmail());
            return token;
        } else {
            throw new AuthenticationException(INVALID_ERR_MSG);
        }
    }

    public User register(User user) {
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
