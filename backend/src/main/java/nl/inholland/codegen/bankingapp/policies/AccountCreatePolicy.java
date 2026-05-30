package nl.inholland.codegen.bankingapp.policies;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Component;

import nl.inholland.codegen.bankingapp.exceptions.IbanNotGenerated;
import nl.inholland.codegen.bankingapp.models.*;
import nl.inholland.codegen.bankingapp.utils.IbanUtil;

@Component
public class AccountCreatePolicy {

    private final IbanUtil ibanUtil;

    public AccountCreatePolicy(IbanUtil ibanUtil) {
		this.ibanUtil = ibanUtil;
	}

	public void enforceAccountCreatePolicy(Account account, User issuer) {
        enforceIssuerIsEmployee(issuer);
        enforceAccountHasAccountNumber(account);
        enforceCheckingAccountHasIban(account);
    }

    public void enforceIssuerIsEmployee(User issuer) {
        if (issuer.getRole() != User.Role.Employee) {
            throw new AuthorizationDeniedException("Only employees can create new accounts");
        }
    }

    public void enforceAccountHasAccountNumber(Account account) {
        if (account.getAccountNumber() == null) throw new IllegalArgumentException("Account number must not be null");
    }

    public void enforceCheckingAccountHasIban(Account account) {
        if (account.getAccountType() == Account.AccountType.Checking) {
            if (account.getIban() == null) throw new IbanNotGenerated();
        }
    }

    public void enforceIbanAndAccountNumberMatch(Account account) {
        if (account.getAccountType() == Account.AccountType.Checking) {
            ibanUtil.matches(account.getAccountNumber(), account.getIban());
        }
    }
}
