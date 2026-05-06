package nl.inholland.codegen.bankingapp.dtos;

public record AccountSummaryResponse (
    long accountId,
    String iban,
    String accountType)
{}
