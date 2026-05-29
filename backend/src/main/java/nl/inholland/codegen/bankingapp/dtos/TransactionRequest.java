package nl.inholland.codegen.bankingapp.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TransactionRequest (

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "from is required")
    String from,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "to is required")
    String to,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @Positive(message = "amountInCents must be positive")
    long amountInCents
) {}
