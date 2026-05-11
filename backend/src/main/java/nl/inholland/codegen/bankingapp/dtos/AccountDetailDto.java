package nl.inholland.codegen.bankingapp.dtos;

public record AccountDetailDto(
    long accountId,
    String iban,
    String accountType,
    int balanceCents,
    long customerId,
    int absoluteLimitCents,
    int dailyLimitCents
) {}
