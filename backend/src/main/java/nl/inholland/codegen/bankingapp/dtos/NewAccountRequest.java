package nl.inholland.codegen.bankingapp.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import nl.inholland.codegen.bankingapp.models.Account;

public record NewAccountRequest(
        @PositiveOrZero(message = "userId must be positive")
        @NotNull(message = "userId cannot be null")
        Long userId,

        @NotNull(message = "accountType cannot be null")
        Account.AccountType accountType,

        Long absoluteLimitInCents,
        @Schema(defaultValue = "500000") // NOTE: KEEP IN SYNC WITH DEFAULT_DAILY_LIMIT
        Long dailyLimitInCents,

        @Schema(defaultValue = "false")
        Boolean closed
) {
    public NewAccountRequest {
        if (absoluteLimitInCents == null) {
            absoluteLimitInCents = Account.DEFAULT_ABSOLUTE_LIMIT;
        }

        if (dailyLimitInCents == null) {
            dailyLimitInCents = Account.DEFAULT_DAILY_LIMIT;
        }

        if (closed == null) {
            closed = false;
        }
    }
}
