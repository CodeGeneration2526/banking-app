package nl.inholland.codegen.bankingapp.dtos;

public record LoginRequestDTO(
    String email,
    String password
) {}
