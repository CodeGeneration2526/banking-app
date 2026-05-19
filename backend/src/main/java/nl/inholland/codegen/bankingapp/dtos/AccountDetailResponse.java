package nl.inholland.codegen.bankingapp.dtos;

public record AccountDetailResponse(
    long accountId,
    String iban,
    String accountType,
    long storedAmountInCents,
    long userId,
    int absoluteLimitInCents,
    int dailyLimitInCents
) {
}
