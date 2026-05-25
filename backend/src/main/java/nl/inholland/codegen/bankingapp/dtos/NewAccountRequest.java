package nl.inholland.codegen.bankingapp.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import nl.inholland.codegen.bankingapp.models.Account;

public record NewAccountRequest(
        @Positive(message = "userId must be positive")
        long userId,
        @NotNull
        Account.AccountType accountType,
        Long absoluteLimitInCents,
        Long dailyLimitInCents,
        Boolean closed
) {
}
