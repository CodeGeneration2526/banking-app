package nl.inholland.codegen.bankingapp.dtos;

public record TransactionRequest (
    String fromIban,
    String toIban,
    long amountInCents,
    long transferredBy)
{}
