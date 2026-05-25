package nl.inholland.codegen.bankingapp.policies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authorization.AuthorizationDeniedException;

import nl.inholland.codegen.bankingapp.exceptions.IbanNotGenerated;
import nl.inholland.codegen.bankingapp.models.Account;
import nl.inholland.codegen.bankingapp.models.User;

import static org.junit.jupiter.api.Assertions.*;

class AccountCreatePolicyTest {
    private AccountCreatePolicy accountCreatePolicy;
    private User employeeUser;
    private User customerUser;
    private Account account;

    @BeforeEach
    void setUp() {
        accountCreatePolicy = new AccountCreatePolicy();

        employeeUser = new User();
        employeeUser.setRole(User.Role.Employee);

        customerUser = new User();
        customerUser.setRole(User.Role.Customer);

        account = new Account();
        account.setAccountType(Account.AccountType.Checking);
    }

    @Test
    void enforceIssuerIsEmployee_throwsWithNonEmployeeIssuer() {
        assertThrows(AuthorizationDeniedException.class,
                () -> accountCreatePolicy.enforceIssuerIsEmployee(customerUser));
    }

    @Test
    void enforceIssuerIsEmployee_successWithEmployeeIssuer() {
        assertDoesNotThrow(() -> accountCreatePolicy.enforceIssuerIsEmployee(employeeUser));
    }

    @Test
    void enforceHasIban_throwsWithNullIban() {
        account.setIban(null);

        assertThrows(IbanNotGenerated.class,
                () -> accountCreatePolicy.enforceHasIban(account));
    }

    @Test
    void enforceHasIban_throwsWithBlankIban() {
        account.setIban("   ");

        assertThrows(IbanNotGenerated.class,
                () -> accountCreatePolicy.enforceHasIban(account));
    }

    @Test
    void enforceHasIban_successWithIban() {
        account.setIban("NL00INHO0000000001");
        assertDoesNotThrow(() -> accountCreatePolicy.enforceHasIban(account));
    }

    @Test
    void enforceAccountCreatePolicy_throwsWhenIssuerNotEmployee() {
        assertThrows(AuthorizationDeniedException.class,
                () -> accountCreatePolicy.enforceAccountCreatePolicy(account, customerUser));
    }
}
