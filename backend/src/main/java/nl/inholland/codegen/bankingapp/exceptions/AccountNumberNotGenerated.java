package nl.inholland.codegen.bankingapp.exceptions;

public class AccountNumberNotGenerated extends RuntimeException {
	public AccountNumberNotGenerated() {
        super("Account number was not generated");
	}
}
