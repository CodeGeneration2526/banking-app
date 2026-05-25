package nl.inholland.codegen.bankingapp.policies;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Component;

import nl.inholland.codegen.bankingapp.exceptions.IbanNotGenerated;
import nl.inholland.codegen.bankingapp.models.*;

@Component
public class AccountCreatePolicy {
    public void enforceAccountCreatePolicy(Account account, User issuer) {
        enforceIssuerIsEmployee(issuer);
        enforceHasIban(account);
    }

    public void enforceIssuerIsEmployee(User issuer) {
        if (issuer.getRole() != User.Role.Employee) {
            throw new AuthorizationDeniedException("Only employees can create new accounts");
        }
    }

    public void enforceHasIban(Account account) {
        if (account.getIban() == null || account.getIban().isBlank()) {
            throw new IbanNotGenerated();
        }
    }
}
