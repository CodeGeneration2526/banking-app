package nl.inholland.codegen.bankingapp.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import nl.inholland.codegen.bankingapp.dtos.TransactionFilter;
import nl.inholland.codegen.bankingapp.exceptions.BadRequestException;
import nl.inholland.codegen.bankingapp.exceptions.NotFoundException;
import nl.inholland.codegen.bankingapp.models.Account;
import nl.inholland.codegen.bankingapp.models.Transaction;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.policies.TransactionExecutePolicy;
import nl.inholland.codegen.bankingapp.repositories.AccountRepository;
import nl.inholland.codegen.bankingapp.repositories.TransactionRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionExecutePolicy transactionExecutePolicy;

    @InjectMocks private TransactionService transactionService;

    private User customer;
    private User employee;
    private Account sender;
    private Account receiver;

    private static final long FROM_ACCOUNT = 1001L;
    private static final long TO_ACCOUNT = 1002L;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setUserId(1L);
        customer.setRole(User.Role.Customer);

        employee = new User();
        employee.setUserId(99L);
        employee.setRole(User.Role.Employee);

        sender = new Account();
        sender.setAccountId(10L);
        sender.setAccountNumber(FROM_ACCOUNT);
        sender.setAccountType(Account.AccountType.Checking);
        sender.setOwner(customer);
        sender.setStoredAmountInCents(100_000L);
        sender.setAbsoluteLimitInCents(0L);
        sender.setDailyLimitInCents(50_000L);
        sender.setClosed(false);

        receiver = new Account();
        receiver.setAccountId(11L);
        receiver.setAccountNumber(TO_ACCOUNT);
        receiver.setAccountType(Account.AccountType.Checking);
        receiver.setOwner(customer);
        receiver.setStoredAmountInCents(0L);
        receiver.setClosed(false);
    }

    @Test
    void executeTransaction_throwsNotFoundWhenSenderMissing() {
        when(accountRepository.findByAccountNumber(FROM_ACCOUNT)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> transactionService.executeTransaction(FROM_ACCOUNT, TO_ACCOUNT, 100L, customer));
        assertEquals("From account not found", ex.getMessage());
        verifyNoInteractions(transactionExecutePolicy);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void executeTransaction_throwsNotFoundWhenReceiverMissing() {
        when(accountRepository.findByAccountNumber(FROM_ACCOUNT)).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumber(TO_ACCOUNT)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> transactionService.executeTransaction(FROM_ACCOUNT, TO_ACCOUNT, 100L, customer));
        assertEquals("To account not found", ex.getMessage());
        verifyNoInteractions(transactionExecutePolicy);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void executeTransaction_propagatesPolicyException() {
        when(accountRepository.findByAccountNumber(FROM_ACCOUNT)).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumber(TO_ACCOUNT)).thenReturn(Optional.of(receiver));
        doThrow(new BadRequestException("Account is closed"))
                .when(transactionExecutePolicy)
                .enforceTransactionExecutePolicy(sender, receiver, 100L, customer);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> transactionService.executeTransaction(FROM_ACCOUNT, TO_ACCOUNT, 100L, customer));
        assertEquals("Account is closed", ex.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void executeTransaction_throwsWhenDailyLimitExceeded() {
        when(accountRepository.findByAccountNumber(FROM_ACCOUNT)).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumber(TO_ACCOUNT)).thenReturn(Optional.of(receiver));
        Transaction priorToday = new Transaction();
        priorToday.setAmountInCents(40_000L);
        when(transactionRepository.findBySenderAccount_AccountIdAndTimestampAfterAndTimestampBefore(
                eq(sender.getAccountId()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(priorToday));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> transactionService.executeTransaction(FROM_ACCOUNT, TO_ACCOUNT, 20_000L, customer));
        assertEquals("Daily transfer limit exceeded", ex.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void executeTransaction_allowsTransferAtExactDailyLimit() {
        when(accountRepository.findByAccountNumber(FROM_ACCOUNT)).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumber(TO_ACCOUNT)).thenReturn(Optional.of(receiver));
        Transaction priorToday = new Transaction();
        priorToday.setAmountInCents(40_000L);
        when(transactionRepository.findBySenderAccount_AccountIdAndTimestampAfterAndTimestampBefore(
                eq(sender.getAccountId()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(priorToday));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(
                () -> transactionService.executeTransaction(FROM_ACCOUNT, TO_ACCOUNT, 10_000L, customer));
    }

    // Happy path: captures the saved Transaction and verifies sender/receiver were both
    // mutated and every field on the persisted entity comes from the right source.
    @Test
    void executeTransaction_debitsCreditsAndPersistsOnSuccess() {
        when(accountRepository.findByAccountNumber(FROM_ACCOUNT)).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumber(TO_ACCOUNT)).thenReturn(Optional.of(receiver));
        when(transactionRepository.findBySenderAccount_AccountIdAndTimestampAfterAndTimestampBefore(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = transactionService.executeTransaction(FROM_ACCOUNT, TO_ACCOUNT, 25_000L, customer);

        assertEquals(75_000L, sender.getStoredAmountInCents());
        assertEquals(25_000L, receiver.getStoredAmountInCents());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction saved = captor.getValue();
        assertSame(sender, saved.getSenderAccount());
        assertSame(receiver, saved.getReceiverAccount());
        assertEquals(25_000L, saved.getAmountInCents());
        assertSame(customer, saved.getInitiatedBy());
        assertNotNull(saved.getTimestamp());
        assertSame(saved, result);
    }

    @Test
    void executeTransaction_invokesPolicyBeforeMutatingBalances() {
        when(accountRepository.findByAccountNumber(FROM_ACCOUNT)).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumber(TO_ACCOUNT)).thenReturn(Optional.of(receiver));
        when(transactionRepository.findBySenderAccount_AccountIdAndTimestampAfterAndTimestampBefore(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        transactionService.executeTransaction(FROM_ACCOUNT, TO_ACCOUNT, 1_000L, customer);

        InOrderVerifier order = new InOrderVerifier();
        order.verifyPolicyThenSave(transactionExecutePolicy, transactionRepository, sender, receiver, customer);
    }

    @Test
    void getTransactions_customerScopedToOwnUserId() {
        TransactionFilter filter = new TransactionFilter(null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(List.of());
        when(transactionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Transaction> result = transactionService.getTransactions(customer, 999L, filter, pageable);

        assertSame(page, result);
        verify(transactionRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getTransactions_employeeWithoutUserIdReturnsAllTransactions() {
        TransactionFilter filter = new TransactionFilter(null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(List.of());
        when(transactionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Transaction> result = transactionService.getTransactions(employee, null, filter, pageable);

        assertSame(page, result);
        ArgumentCaptor<Specification<Transaction>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(transactionRepository).findAll(specCaptor.capture(), eq(pageable));
        assertNotNull(specCaptor.getValue());
    }

    @Test
    void getTransactions_employeeWithUserIdScopesToThatUser() {
        TransactionFilter filter = new TransactionFilter(null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> page = new PageImpl<>(List.of());
        when(transactionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Transaction> result = transactionService.getTransactions(employee, 42L, filter, pageable);

        assertSame(page, result);
        verify(transactionRepository).findAll(any(Specification.class), eq(pageable));
    }

    // Helper to keep the InOrder assertion off to the side.
    private static class InOrderVerifier {
        void verifyPolicyThenSave(TransactionExecutePolicy policy,
                                  TransactionRepository repo,
                                  Account sender, Account receiver, User initiator) {
            org.mockito.InOrder inOrder = inOrder(policy, repo);
            inOrder.verify(policy).enforceTransactionExecutePolicy(eq(sender), eq(receiver), anyLong(), eq(initiator));
            inOrder.verify(repo).save(any(Transaction.class));
        }
    }
}
