package nl.inholland.codegen.bankingapp.dtos;

public record CustomerLookupResponse (
    long userId,
    String firstName,
    String lastName,
    String iban)
{}
