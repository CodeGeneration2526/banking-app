package nl.inholland.codegen.bankingapp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import nl.inholland.codegen.bankingapp.dtos.LoginRequest;
import nl.inholland.codegen.bankingapp.dtos.LoginResponse;
import nl.inholland.codegen.bankingapp.dtos.RegisterRequest;
import nl.inholland.codegen.bankingapp.dtos.UserResponse;
import nl.inholland.codegen.bankingapp.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Login and registration endpoints")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate and receive a JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("/register")
    @Operation(summary = "Register", description = "Register a new customer. Account requires employee approval.")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        // return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerCustomer(request));
    }
}
