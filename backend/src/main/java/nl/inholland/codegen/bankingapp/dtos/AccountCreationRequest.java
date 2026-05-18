package nl.inholland.codegen.bankingapp.dtos;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record AccountCreationRequest (

    @Positive(message = "userId must be positive")
    long userId,

    long absoluteLimitInCents,

    @PositiveOrZero(message = "Daily limit cannot be negative")
    long dailyLimitInCents
) {}
