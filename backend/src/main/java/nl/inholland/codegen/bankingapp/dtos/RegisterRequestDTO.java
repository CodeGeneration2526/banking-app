package nl.inholland.codegen.bankingapp.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    String lastName,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).+$",
             message = "Password must contain at least one uppercase letter and one number")
    String password,

    @NotBlank(message = "BSN is required")
    @Pattern(regexp = "^[0-9]{8,9}$", message = "BSN must be 8 or 9 digits")
    String bsn,

    @Pattern(regexp = "^(\\+?[0-9]{10,15})?$", message = "Invalid phone number format")
    String phoneNumber
) {}
