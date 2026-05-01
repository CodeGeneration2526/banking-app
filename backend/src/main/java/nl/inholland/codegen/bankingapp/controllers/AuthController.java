package nl.inholland.codegen.bankingapp.controllers;

import nl.inholland.codegen.bankingapp.dtos.*;
import nl.inholland.codegen.bankingapp.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@RequestBody LoginRequestDTO request) {

        LoginResponseDTO response = userService.login(request);

        return ResponseEntity.ok(
            new ApiResponse<>(
                "Login successful",
                200,
                response
            )
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(@RequestBody RegisterRequestDTO request) {

        UserResponseDTO user = userService.registerCustomer(request);

        return ResponseEntity.status(201).body(
            new ApiResponse<>(
                "Registration successful. Awaiting employee approval.",
                201,
                user
            )
        );
    }
}