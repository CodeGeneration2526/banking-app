package nl.inholland.codegen.bankingapp.services;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;

import nl.inholland.codegen.bankingapp.exceptions.BadRequestException;
import nl.inholland.codegen.bankingapp.exceptions.NotFoundException;
import nl.inholland.codegen.bankingapp.models.Account;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.policies.AccountCreatePolicy;
import nl.inholland.codegen.bankingapp.policies.ApproveUsersPolicy;
import nl.inholland.codegen.bankingapp.repositories.AccountRepository;
import nl.inholland.codegen.bankingapp.repositories.UserRepository;
import nl.inholland.codegen.bankingapp.utils.IbanUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private AccountCreatePolicy accountCreatePolicy;
    @Mock private ApproveUsersPolicy approveUsersPolicy;
    @Mock private IbanUtil ibanUtil;

    @InjectMocks private AccountService accountService;

    private User customer;
    private User employee;
    private Account checkingAccount;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setUserId(1L);
        customer.setRole(User.Role.Customer);
        customer.setClosed(false);

        employee = new User();
        employee.setUserId(99L);
        employee.setRole(User.Role.Employee);
        employee.setClosed(false);

        checkingAccount = new Account();
        checkingAccount.setAccountId(10L);
        checkingAccount.setAccountNumber(1000000001L);
        checkingAccount.setAccountType(Account.AccountType.Checking);
        checkingAccount.setOwner(customer);
        checkingAccount.setStoredAmountInCents(50_000L);
        checkingAccount.setClosed(false);
    }

    @Test
    void getAllAccounts_returnsPagedResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> page = new PageImpl<>(java.util.List.of(checkingAccount));
        Specification<Account> spec = (root, query, criteriaBuilder) -> null;

        when(accountRepository.findAll(eq(spec), eq(pageable))).thenReturn(page);

        Page<Account> result = accountService.getAllAccounts(spec, pageable);

        assertSame(page, result);
        verify(accountRepository).findAll(eq(spec), eq(pageable));
    }

    @Test
    void getAccountInfo_returnsAccount_whenExists() {
        when(accountRepository.findByAccountId(10L)).thenReturn(Optional.of(checkingAccount));

        Optional<Account> result = accountService.getAccountInfo(10L);

        assertTrue(result.isPresent());
        assertSame(checkingAccount, result.get());
    }

    @Test
    void getAccountInfo_returnsEmpty_whenNotExists() {
        when(accountRepository.findByAccountId(999L)).thenReturn(Optional.empty());

        Optional<Account> result = accountService.getAccountInfo(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void closeAccount_setsClosedToTrue() {
        when(accountRepository.findByAccountId(10L)).thenReturn(Optional.of(checkingAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        accountService.closeAccount(10L);

        assertTrue(checkingAccount.getClosed());
        verify(accountRepository).save(checkingAccount);
    }

    @Test
    void closeAccount_throwsNotFoundException_whenAccountNotExists() {
        when(accountRepository.findByAccountId(999L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> accountService.closeAccount(999L));
        assertEquals("Account with the given account ID could not be found.", ex.getMessage());
    }

    @Test
    void createAccount_generatesAccountNumberAndIbanForChecking() {
        when(ibanUtil.newAccountNumber()).thenReturn(2000000001L);
        when(ibanUtil.generateIban(2000000001L)).thenReturn("NL00INHO2000000001");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setAccountId(20L);
            return a;
        });

        Account newAccount = new Account();
        newAccount.setAccountType(Account.AccountType.Checking);
        newAccount.setOwner(customer);

        Account result = accountService.createAccount(newAccount, employee);

        assertEquals(2000000001L, result.getAccountNumber());
        assertEquals("NL00INHO2000000001", result.getIban());
        verify(accountCreatePolicy).enforceAccountCreatePolicy(result, employee);
        verify(accountRepository).save(result);
    }

    @Test
    void createAccount_generatesAccountNumberWithoutIbanForSavings() {
        when(ibanUtil.newAccountNumber()).thenReturn(3000000001L);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setAccountId(30L);
            return a;
        });

        Account newAccount = new Account();
        newAccount.setAccountType(Account.AccountType.Savings);
        newAccount.setOwner(customer);

        Account result = accountService.createAccount(newAccount, employee);

        assertEquals(3000000001L, result.getAccountNumber());
        assertNull(result.getIban());
        verify(accountCreatePolicy).enforceAccountCreatePolicy(result, employee);
    }

    @Test
    void createAccount_propagatesPolicyException() {
        when(ibanUtil.newAccountNumber()).thenReturn(4000000001L);
        doThrow(new AuthorizationDeniedException("Only employees can create new accounts"))
                .when(accountCreatePolicy).enforceAccountCreatePolicy(any(), any());

        Account newAccount = new Account();
        newAccount.setAccountType(Account.AccountType.Checking);

        assertThrows(AuthorizationDeniedException.class,
                () -> accountService.createAccount(newAccount, customer));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void approveAndCreateAccounts_approvesUserAndCreatesTwoAccounts() {
        when(ibanUtil.newAccountNumber()).thenReturn(5000000001L, 5000000002L);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setAccountId(System.nanoTime());
            return a;
        });

        accountService.approveAndCreateAccounts(customer, employee, 0L, 500_000L);

        verify(approveUsersPolicy).enforceApproveUsersPolicy(customer, employee);
        verify(userRepository).save(customer);
        assertNotNull(customer.getApprovedBy());
        assertSame(employee, customer.getApprovedBy());
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void approveAndCreateAccounts_throwsWhenPolicyDeniesApproval() {
        doThrow(new BadRequestException("Customer already approved"))
                .when(approveUsersPolicy).enforceApproveUsersPolicy(any(), any());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> accountService.approveAndCreateAccounts(customer, employee, 0L, 500_000L));
        assertEquals("Customer already approved", ex.getMessage());
        verify(accountRepository, never()).save(any());
    }
}
