package nl.inholland.codegen.bankingapp.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.repositories.AccountRepository;
import nl.inholland.codegen.bankingapp.repositories.UserRepository;
import nl.inholland.codegen.bankingapp.services.AccountService;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper accountMapper;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;

    @Test
    void listAllAccounts_WithNoFilters_ReturnsPagedAccounts() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void listAllAccounts_WithIbanFilter_ReturnsFilteredAccounts() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void listAllAccounts_WithFirstNameFilter_ReturnsFilteredAccounts() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void listAllAccounts_WithLastNameFilter_ReturnsFilteredAccounts() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void getAccountInfo_WithValidId_ReturnsAccount() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void getAccountInfo_WithInvalidId_ReturnsNotFound() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void closeAccount_WithValidId_ReturnsNoContent() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void closeAccount_WithInvalidId_ReturnsNotFound() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void updateAccount_ReturnsNotImplemented() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }
}
