package nl.inholland.codegen.bankingapp.dtos;

public record CustomerLookupDto(
    long customerId,
    String firstName,
    String lastName,
    String iban
) {}
