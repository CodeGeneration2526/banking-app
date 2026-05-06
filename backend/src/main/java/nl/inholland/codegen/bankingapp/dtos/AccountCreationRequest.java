package nl.inholland.codegen.bankingapp.dtos;

public record AccountCreationRequest (
     long userId,
     long absoluteLimitInCents,
     long dailyLimitInCents)
{}
