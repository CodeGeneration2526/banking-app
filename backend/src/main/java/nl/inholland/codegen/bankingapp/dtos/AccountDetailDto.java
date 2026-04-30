package nl.inholland.codegen.bankingapp.dtos;

public class AccountDetailDto {
    public long accountId;
    public String iban;
    public String accountType;
    public int balanceCents;
    public long customerId;
    public int absoluteLimitCents;
    public int dailyLimitCents;
}
