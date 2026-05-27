package nl.inholland.codegen.bankingapp.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TransactionRequest (

    @NotBlank(message = "from is required")
    String from,

    @NotBlank(message = "to is required")
    String to,

    @Positive(message = "amountInCents must be positive")
    long amountInCents)
{}
