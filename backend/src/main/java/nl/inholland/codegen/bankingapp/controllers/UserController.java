package nl.inholland.codegen.bankingapp.controllers;

import nl.inholland.codegen.bankingapp.dtos.*;
import nl.inholland.codegen.bankingapp.exceptions.AuthenticationException;
import nl.inholland.codegen.bankingapp.exceptions.NotFoundException;
import nl.inholland.codegen.bankingapp.mappers.UserMapper;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.services.UserService;
import nl.inholland.codegen.bankingapp.utils.GetAuthUser;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/users")
@Tag(name = "User", description = "User account and transaction endpoints")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final GetAuthUser getAuthUser;

    public UserController(UserService userService, UserMapper userMapper, GetAuthUser getAuthUser) {
        this.userService = userService;
		this.userMapper = userMapper;
		this.getAuthUser = getAuthUser;
    }


    @GetMapping
    @Operation(summary = "Get all users", description = "Returns all user accounts.")
    @PreAuthorize("hasRole('Employee')")
    public ResponseEntity<PagedModel<UserResponse>> getAllUsers(
            @RequestParam(required = false) Boolean isApproved,
            @RequestParam(required = false) User.Role role,
            @ParameterObject @PageableDefault(size = 10, sort = "registrationDate", direction = Sort.Direction.ASC) Pageable pageable
    ) {

        // I can use a specification here, but this is truly very simple logic and complexity is not needed at this point
        // if I add ONE more filter option, then I will add a specificatoion
        Page<UserResponse> response;
        if (role == null) {
            response = userService.getAllUsers(isApproved, pageable).map(userMapper::toUserResponse);
        } else {
            response = userService.getAllUsers(isApproved, role, pageable).map(userMapper::toUserResponse);
        }

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
        User authUser = getAuthUser.getAuthUser().orElseThrow(() -> new AuthenticationException());

        // this will likely be the same as authUser, but it could very well not be in some cases
        User user = userService.getUser(authUser.getUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));

        UserResponse userResponse = userMapper.toUserResponse(user);

        return ResponseEntity.ok(userResponse);
    }

    @PatchMapping("{userId}")
    @Operation(summary = "Update specific user", description = "Update values for a specific user.")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable long userId,
            @Valid @RequestBody UserPatchRequest request) {
        User authUser = getAuthUser.getAuthUser().orElseThrow(() -> new AuthenticationException());

        if (authUser.getRole() != User.Role.Employee && authUser.getUserId() != userId) {
            throw new AuthorizationDeniedException("Not authorized to update this user");
        }

        User updated = userService.updateUser(userId, request);
        return ResponseEntity.ok(userMapper.toUserResponse(updated));
    }

}
