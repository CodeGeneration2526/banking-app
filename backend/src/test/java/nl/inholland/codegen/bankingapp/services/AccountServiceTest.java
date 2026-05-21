package nl.inholland.codegen.bankingapp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import nl.inholland.codegen.bankingapp.repositories.AccountRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void searchCheckingAccounts_WithNoFilters_ReturnsAllAccounts() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void searchCheckingAccounts_WithIbanFilter_ReturnsFilteredAccounts() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void searchCheckingAccounts_WithFirstNameFilter_ReturnsFilteredAccounts() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void searchCheckingAccounts_WithLastNameFilter_ReturnsFilteredAccounts() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void getAccountInfo_WithValidId_ReturnsAccount() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void getAccountInfo_WithInvalidId_ReturnsEmpty() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void closeAccount_WithValidId_ClosesAccount() {
        throw new IllegalStateException("Unimplemented");
    }
}
