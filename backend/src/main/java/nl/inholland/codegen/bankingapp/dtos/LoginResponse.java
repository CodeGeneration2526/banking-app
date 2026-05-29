package nl.inholland.codegen.bankingapp.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse (

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String token
) {}
