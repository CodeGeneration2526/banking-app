package nl.inholland.codegen.bankingapp.dtos;

import nl.inholland.codegen.bankingapp.models.User;

public record UserResponse(
    Long userId,
    String firstName,
    String lastName,
    String email,
    User.Role role
) {}
