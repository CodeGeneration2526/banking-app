package nl.inholland.codegen.bankingapp.dtos;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;

import nl.inholland.codegen.bankingapp.models.User;

public record UserResponse (

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    long userId,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String firstName,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String lastName,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String email,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String phoneNumber,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String bsn,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    User.Role role,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Date registrationDate,

    Long approvedBy,

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean closed
) {}
