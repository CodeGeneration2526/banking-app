package nl.inholland.codegen.bankingapp.dtos;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UserPatchRequest (

    @Positive(message = "userId must be positive")
    long userId,

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    String firstName,

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    String lastName
) {}
