package nl.inholland.codegen.bankingapp.dtos;

public record UpdateAccountRequest (
    long absoluteLimitInCents,
    long dailyLimitInCents,
    Boolean closed)
{}
