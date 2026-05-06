package nl.inholland.codegen.bankingapp.dtos;

import java.time.LocalDateTime;

public record TransactionResponse (
    long transactionId,
    String fromIban,
    String toIban,
    int amountInCents,
    LocalDateTime timestamp,
    String transferredBy)
{}
