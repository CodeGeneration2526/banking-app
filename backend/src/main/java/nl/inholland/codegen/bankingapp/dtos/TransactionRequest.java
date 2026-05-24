package nl.inholland.codegen.bankingapp.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TransactionRequest (

    @NotBlank(message = "fromIban is required")
    String fromIban,

    @NotBlank(message = "toIban is required")
    String toIban,

    @Positive(message = "amountInCents must be positive")
    long amountInCents)
{}
