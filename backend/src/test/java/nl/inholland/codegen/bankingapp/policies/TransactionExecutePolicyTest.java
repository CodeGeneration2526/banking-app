package nl.inholland.codegen.bankingapp.policies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.inholland.codegen.bankingapp.exceptions.BadRequestException;
import nl.inholland.codegen.bankingapp.models.Account;
import nl.inholland.codegen.bankingapp.models.User;

import static org.junit.jupiter.api.Assertions.*;

class TransactionExecutePolicyTest {
    private TransactionExecutePolicy policy;
    private User employee;
    private User customer;
    private User otherCustomer;
    private Account sender;
    private Account receiver;
    private Account savings;

    @BeforeEach
    void setUp() {
        policy = new TransactionExecutePolicy();

        employee = new User();
        employee.setUserId(99L);
        employee.setRole(User.Role.Employee);

        customer = new User();
        customer.setUserId(1L);
        customer.setRole(User.Role.Customer);

        otherCustomer = new User();
        otherCustomer.setUserId(2L);
        otherCustomer.setRole(User.Role.Customer);

        sender = new Account();
        sender.setAccountType(Account.AccountType.Checking);
        sender.setOwner(customer);
        sender.setStoredAmountInCents(100_000L);
        sender.setAbsoluteLimitInCents(0L);
        sender.setClosed(false);

        receiver = new Account();
        receiver.setAccountType(Account.AccountType.Checking);
        receiver.setOwner(customer);
        receiver.setClosed(false);

        savings = new Account();
        savings.setAccountType(Account.AccountType.Savings);
        savings.setOwner(customer);
        savings.setClosed(false);
    }

    @Test
    void enforceAccountsNotClosed_throwsWhenSenderClosed() {
        sender.setClosed(true);

        assertThrows(BadRequestException.class,
                () -> policy.enforceAccountsNotClosed(sender, receiver));
    }

    @Test
    void enforceAccountsNotClosed_throwsWhenReceiverClosed() {
        receiver.setClosed(true);

        assertThrows(BadRequestException.class,
                () -> policy.enforceAccountsNotClosed(sender, receiver));
    }

    @Test
    void enforceAccountsNotClosed_successWhenBothOpen() {
        assertDoesNotThrow(() -> policy.enforceAccountsNotClosed(sender, receiver));
    }

    @Test
    void enforceInitiatorCanTransferFromSender_successWhenEmployeeInitiator() {
        // Employees can move funds between any accounts, even ones they don't own.
        sender.setOwner(otherCustomer);

        assertDoesNotThrow(() -> policy.enforceInitiatorCanTransferFromSender(employee, sender));
    }

    @Test
    void enforceInitiatorCanTransferFromSender_throwsWhenCustomerNotOwner() {
        assertThrows(BadRequestException.class,
                () -> policy.enforceInitiatorCanTransferFromSender(otherCustomer, sender));
    }

    @Test
    void enforceInitiatorCanTransferFromSender_successWhenCustomerIsOwner() {
        assertDoesNotThrow(() -> policy.enforceInitiatorCanTransferFromSender(customer, sender));
    }

    @Test
    void enforceSavingsTransferRule_successWhenEmployeeInitiator() {
        // Employee bypass applies even when the savings rule would otherwise reject.
        receiver.setOwner(otherCustomer);

        assertDoesNotThrow(() -> policy.enforceSavingsTransferRule(employee, savings, receiver));
    }

    @Test
    void enforceSavingsTransferRule_throwsWhenSenderIsSavingsAndReceiverOwnedByOther() {
        receiver.setOwner(otherCustomer);

        assertThrows(BadRequestException.class,
                () -> policy.enforceSavingsTransferRule(customer, savings, receiver));
    }

    @Test
    void enforceSavingsTransferRule_throwsWhenReceiverIsSavingsAndOwnedByOther() {
        savings.setOwner(otherCustomer);

        assertThrows(BadRequestException.class,
                () -> policy.enforceSavingsTransferRule(customer, sender, savings));
    }

    @Test
    void enforceSavingsTransferRule_successWhenSavingsBetweenOwnAccounts() {
        assertDoesNotThrow(() -> policy.enforceSavingsTransferRule(customer, savings, receiver));
    }

    @Test
    void enforceSavingsTransferRule_successWhenNoSavingsInvolved() {
        receiver.setOwner(otherCustomer);

        assertDoesNotThrow(() -> policy.enforceSavingsTransferRule(customer, sender, receiver));
    }

    @Test
    void enforceAbsoluteLimit_throwsWhenAmountWouldBreachLimit() {
        sender.setStoredAmountInCents(100L);
        sender.setAbsoluteLimitInCents(0L);

        assertThrows(BadRequestException.class,
                () -> policy.enforceAbsoluteLimit(sender, 200L));
    }

    @Test
    void enforceAbsoluteLimit_throwsWhenExactlyAtLimitBoundary() {
        sender.setStoredAmountInCents(100L);
        sender.setAbsoluteLimitInCents(1L);

        assertThrows(BadRequestException.class,
                () -> policy.enforceAbsoluteLimit(sender, 100L));
    }

    @Test
    void enforceAbsoluteLimit_successWhenAmountWithinLimit() {
        sender.setStoredAmountInCents(100L);
        sender.setAbsoluteLimitInCents(0L);

        assertDoesNotThrow(() -> policy.enforceAbsoluteLimit(sender, 50L));
    }

    @Test
    void enforceTransactionExecutePolicy_throwsWhenSenderClosed() {
        sender.setClosed(true);

        assertThrows(BadRequestException.class,
                () -> policy.enforceTransactionExecutePolicy(sender, receiver, 100L, customer));
    }

    @Test
    void enforceTransactionExecutePolicy_throwsWhenCustomerNotOwner() {
        assertThrows(BadRequestException.class,
                () -> policy.enforceTransactionExecutePolicy(sender, receiver, 100L, otherCustomer));
    }

    @Test
    void enforceTransactionExecutePolicy_throwsWhenSavingsRuleViolated() {
        receiver.setOwner(otherCustomer);

        assertThrows(BadRequestException.class,
                () -> policy.enforceTransactionExecutePolicy(savings, receiver, 100L, customer));
    }

    @Test
    void enforceTransactionExecutePolicy_throwsWhenAbsoluteLimitBreached() {
        sender.setStoredAmountInCents(100L);
        sender.setAbsoluteLimitInCents(0L);

        assertThrows(BadRequestException.class,
                () -> policy.enforceTransactionExecutePolicy(sender, receiver, 200L, customer));
    }

    @Test
    void enforceTransactionExecutePolicy_successWithCustomerOwnAccounts() {
        assertDoesNotThrow(() -> policy.enforceTransactionExecutePolicy(sender, receiver, 100L, customer));
    }

    @Test
    void enforceTransactionExecutePolicy_successWithEmployeeInitiator() {
        sender.setOwner(otherCustomer);
        receiver.setOwner(otherCustomer);

        assertDoesNotThrow(() -> policy.enforceTransactionExecutePolicy(sender, receiver, 100L, employee));
    }
}
