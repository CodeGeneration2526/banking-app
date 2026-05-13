package nl.inholland.codegen.bankingapp.dtos;

import java.time.LocalDateTime;

public record UserResponse(
    long userId,
    String firstName,
    String lastName,
    String email,
    int phoneNumber,
    int bsn,
    LocalDateTime registrationDate)
{}
