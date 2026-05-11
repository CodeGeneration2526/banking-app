package nl.inholland.codegen.bankingapp.dtos;

public record AccountSummaryDto(
    long accountId,
    String iban,
    String accountType,
    int balanceCents
) {}
