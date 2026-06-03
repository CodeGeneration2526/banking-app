package nl.inholland.codegen.bankingapp.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

public record ApiResponse (

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String message
) {}
