package nl.inholland.codegen.bankingapp.controllers;

import nl.inholland.codegen.bankingapp.dtos.*;
import nl.inholland.codegen.bankingapp.exceptions.AuthenticationException;
import nl.inholland.codegen.bankingapp.exceptions.NotFoundException;
import nl.inholland.codegen.bankingapp.mappers.UserMapper;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.services.UserService;

import java.util.Optional;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/users")
@Tag(name = "User", description = "User account and transaction endpoints")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
		this.userMapper = userMapper;
    }


    @GetMapping
    @Operation(summary = "Get all users", description = "Returns all user accounts.")
    @PreAuthorize("hasRole('Employee')")
    public ResponseEntity<PagedModel<UserResponse>> getAllUsers(
            @ParameterObject @PageableDefault(size = 10, sort = "registrationDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<UserResponse> response = userService.getAllUsers(true, pageable).map(userMapper::toUserResponse);
        return ResponseEntity.ok(new PagedModel<>(response));
    }

    @GetMapping("{userId}")
    @Operation(summary = "Get one user", description = "Returns just one user from the given ID.")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        UserResponse userResponse = userService.getUser(userId)
            .map(userMapper::toUserResponse)
            .orElseThrow(() -> new NotFoundException("User with the specified ID could not be found"));
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("me")
    @Operation(summary = "Get one user", description = "Returns info on the logged in user.")
    public ResponseEntity<UserResponse> getSelfUser() {
        User authUser = getAuthUser().orElseThrow(() -> new AuthenticationException());

        // this will likely be the same as authUser, but it could very well not be in some cases
        User user = userService.getUser(authUser.getUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));

        UserResponse userResponse = userMapper.toUserResponse(user);

        return ResponseEntity.ok(userResponse);
    }

    @PostMapping
    @Operation(summary = "Approve customer and create accounts", description = "Creates a checking and savings account for the given customer")
    public ResponseEntity<Void> createAccounts(@RequestBody AccountCreationRequest request) {
        User user = getAuthUser().orElseThrow(() -> new AuthenticationException());
        userService.createAccounts(request, user); // TODO: auth needs to be unborked
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PatchMapping("{userId}")
    @Operation(summary = "Update specific user", description = "Update values for a specific user.")
    public ResponseEntity<Void> updateUser(@RequestBody UserPatchRequest request) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    @DeleteMapping("{userId}")
    @Operation(summary = "Delete specific user", description = "Deletes a specific user, archiving their account.")
    public ResponseEntity<Void> deleteUser() {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    private Optional<User> getAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return Optional.empty();
        }

        User user = (User)authentication.getPrincipal();
        return Optional.of(user);
    }
}
