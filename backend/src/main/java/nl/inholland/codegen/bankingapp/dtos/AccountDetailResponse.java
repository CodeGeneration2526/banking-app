package nl.inholland.codegen.bankingapp.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

public record AccountDetailResponse (

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long accountId,

    String iban,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String accountType,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long storedAmountInCents,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long userId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int absoluteLimitInCents,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    int dailyLimitInCents,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean closed
) {}
