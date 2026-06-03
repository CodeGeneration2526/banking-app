package nl.inholland.codegen.bankingapp.dtos;

import jakarta.validation.constraints.PositiveOrZero;

public record UpdateAccountRequest (
    Long absoluteLimitInCents,

    @PositiveOrZero(message = "Daily limit cannot be negative")
    Long dailyLimitInCents,

    Boolean closed
) {}
