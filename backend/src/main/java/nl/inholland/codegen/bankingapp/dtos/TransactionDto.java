package nl.inholland.codegen.bankingapp.dtos;

import java.time.OffsetDateTime;

public record TransactionDto(
    long transactionId,
    String fromIban,
    String toIban,
    int amountCents,
    OffsetDateTime timestamp,
    String initiatedBy
) {}
