package nl.inholland.codegen.bankingapp.dtos;

public record LoginResponseDTO(
    String token,
    String role
) {}
