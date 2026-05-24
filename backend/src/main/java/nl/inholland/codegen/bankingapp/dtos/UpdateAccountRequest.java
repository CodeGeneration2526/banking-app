package nl.inholland.codegen.bankingapp.dtos;

import jakarta.validation.constraints.PositiveOrZero;

public record UpdateAccountRequest (
    long absoluteLimitInCents,

    @PositiveOrZero(message = "Daily limit cannot be negative")
    long dailyLimitInCents,

    Boolean closed
) {}
