package nl.inholland.codegen.bankingapp.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import nl.inholland.codegen.bankingapp.models.Account;

public record NewAccountRequest (

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @PositiveOrZero(message = "userId must be positive")
    @NotNull(message = "userId cannot be null")
    Long userId,

    Long absoluteLimitInCents,

    @Positive(message = "Daily limit cannot be negative")
    @Schema(defaultValue = "50000") // Same as DEFAULT_DAILY_LIMIT
    Long dailyLimitInCents
) {
    public NewAccountRequest {
        if (absoluteLimitInCents == null) {
            absoluteLimitInCents = Account.DEFAULT_ABSOLUTE_LIMIT;
        }

        if (dailyLimitInCents == null) {
            dailyLimitInCents = Account.DEFAULT_DAILY_LIMIT;
        }
    }
}
