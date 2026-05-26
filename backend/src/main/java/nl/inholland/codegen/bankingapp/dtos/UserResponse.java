package nl.inholland.codegen.bankingapp.dtos;

import java.util.Date;

import nl.inholland.codegen.bankingapp.models.User;

public record UserResponse(
    long userId,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    int bsn,
    User.Role role,
    Date registrationDate)
{}
