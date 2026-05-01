package nl.inholland.codegen.bankingapp.dtos;

public record RegisterRequestDTO(
    String firstName,
    String lastName,
    String email,
    String password,
    String bsn,
    String phoneNumber
) {}