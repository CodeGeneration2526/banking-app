package nl.inholland.codegen.bankingapp.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

public record AccountSummaryResponse(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long accountId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long accountNumber,

    String iban,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String ownerFirstName,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String ownerLastName,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long ownerUserId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String accountType
) {}
