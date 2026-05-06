package nl.inholland.codegen.bankingapp.dtos;

public record CreateAccountRequest (
     long userId,
     long absoluteLimitInCents,
     long dailyLimitInCents)
{}
