package nl.inholland.codegen.bankingapp.dtos;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record TransactionResponse (

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long transactionId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String from,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String to,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long amountInCents,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime timestamp,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String initiatedBy
) {}
