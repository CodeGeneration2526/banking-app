package nl.inholland.codegen.bankingapp.exceptions;

public class IbanNotGenerated extends RuntimeException {
    public IbanNotGenerated() {
        super("IBAN was not generated");
    }
}
