package nl.inholland.codegen.bankingapp.dtos;

import java.time.OffsetDateTime;

public class TransactionDto {
    public long transactionId;
    public String fromIban;
    public String toIban;
    public int amountCents;
    public OffsetDateTime timestamp;
    public String initiatedBy;
}
