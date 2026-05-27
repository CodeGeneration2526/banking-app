package nl.inholland.codegen.bankingapp.dtos;

import java.time.LocalDateTime;

public record TransactionResponse (
    long transactionId,
    String from,
    String to,
    long amountInCents,
    LocalDateTime timestamp,
    String initiatedBy)
{}
