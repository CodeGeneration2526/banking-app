package nl.inholland.codegen.bankingapp.dtos;

public class TransferRequestDto {
    public String fromIban;
    public String toIban;
    public int amountCents;
    public long transferredBy;
}
