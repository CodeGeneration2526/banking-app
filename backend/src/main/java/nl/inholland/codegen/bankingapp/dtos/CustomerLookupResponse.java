package nl.inholland.codegen.bankingapp.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

public record CustomerLookupResponse (

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long userId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String firstName,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String lastName,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String iban
) {}
