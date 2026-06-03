package nl.inholland.codegen.bankingapp.policies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authorization.AuthorizationDeniedException;

import nl.inholland.codegen.bankingapp.exceptions.IbanNotGenerated;
import nl.inholland.codegen.bankingapp.models.Account;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.utils.IbanUtil;

import static org.junit.jupiter.api.Assertions.*;

class AccountCreatePolicyTest {
    private AccountCreatePolicy accountCreatePolicy;
    private User employeeUser;
    private User customerUser;
    private Account account;

    @BeforeEach
    void setUp() {
        IbanUtil ibanUtil = new IbanUtil();
        accountCreatePolicy = new AccountCreatePolicy(ibanUtil);

        employeeUser = new User();
        employeeUser.setRole(User.Role.Employee);

        customerUser = new User();
        customerUser.setRole(User.Role.Customer);

        account = new Account();
        account.setAccountType(Account.AccountType.Checking);
        account.setAccountNumber(1L);
        account.setIban("NL00INHO0000000001");
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
    void enforceAccountHasAccountNumber_throwsWithNullAccountNumber() {
        account.setAccountNumber(null);

        assertThrows(IllegalArgumentException.class,
                () -> accountCreatePolicy.enforceAccountHasAccountNumber(account));
    }

    @Test
    void enforceAccountHasAccountNumber_successWithAccountNumber() {
        account.setAccountNumber(1L);

        assertDoesNotThrow(() -> accountCreatePolicy.enforceAccountHasAccountNumber(account));
    }

    @Test
    void enforceCheckingAccountHasIban_throwsWithNullIban() {
        account.setIban(null);

        assertThrows(IbanNotGenerated.class,
                () -> accountCreatePolicy.enforceCheckingAccountHasIban(account));
    }

    @Test
    void enforceCheckingAccountHasIban_successWithIban() {
        account.setIban("NL00INHO0000000001");

        assertDoesNotThrow(() -> accountCreatePolicy.enforceCheckingAccountHasIban(account));
    }

    @Test
    void enforceAccountCreatePolicy_throwsWhenIssuerNotEmployee() {
        assertThrows(AuthorizationDeniedException.class,
                () -> accountCreatePolicy.enforceAccountCreatePolicy(account, customerUser));
    }

    @Test
    void enforceAccountCreatePolicy_throwsWhenAccountNumberMissing() {
        account.setAccountNumber(null);

        assertThrows(IllegalArgumentException.class,
                () -> accountCreatePolicy.enforceAccountCreatePolicy(account, employeeUser));
    }

    @Test
    void enforceAccountCreatePolicy_throwsWhenCheckingAccountIbanMissing() {
        account.setIban(null);

        assertThrows(IbanNotGenerated.class,
                () -> accountCreatePolicy.enforceAccountCreatePolicy(account, employeeUser));
    }

    @Test
    void enforceAccountCreatePolicy_successWithEmployeeAndValidAccount() {
        assertDoesNotThrow(() -> accountCreatePolicy.enforceAccountCreatePolicy(account, employeeUser));
    }
}
