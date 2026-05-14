package nl.inholland.codegen.bankingapp.dtos;

import java.util.Date;

public record UserResponse(
    long userId,
    String firstName,
    String lastName,
    String email,
    int phoneNumber,
    int bsn,
    Date registrationDate)
{}
