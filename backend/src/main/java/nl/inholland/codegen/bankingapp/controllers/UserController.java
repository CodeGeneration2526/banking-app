package nl.inholland.codegen.bankingapp.controllers;

import nl.inholland.codegen.bankingapp.dtos.*;
import nl.inholland.codegen.bankingapp.exceptions.AuthenticationException;
import nl.inholland.codegen.bankingapp.mappers.UserMapper;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.services.UserService;
import nl.inholland.codegen.bankingapp.utils.PaginatedList;
import org.springframework.http.*;
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


    @GetMapping("/search")
    @Operation(summary = "Search customers", description = "Lookup IBANs by customer name or IBAN.")
    public ResponseEntity<PaginatedList<CustomerLookupResponse>> searchCustomers(
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Returns all user accounts.")
    public ResponseEntity<PaginatedList<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("{userId}")
    @Operation(summary = "Get one user", description = "Returns just one user from the given ID.")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping("me")
    @Operation(summary = "Get one user", description = "Returns info on the logged in user.")
    public ResponseEntity<UserResponse> getSelfUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new AuthenticationException();
        }

        User user = (User)authentication.getPrincipal();
        UserResponse userResponse = userMapper.toUserResponse(user);

        System.out.println(user.getEmail());

        return ResponseEntity.ok(userResponse);
    }

    @PostMapping
    @Operation(summary = "Approve customer and create accounts", description = "Creates a checking and savings account for the given customer")
    public ResponseEntity<Void> createAccounts(@RequestBody AccountCreationRequest request) {
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
}
