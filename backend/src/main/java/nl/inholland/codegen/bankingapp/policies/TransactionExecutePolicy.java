package nl.inholland.codegen.bankingapp.policies;

import org.springframework.stereotype.Component;

import nl.inholland.codegen.bankingapp.exceptions.BadRequestException;
import nl.inholland.codegen.bankingapp.models.Account;
import nl.inholland.codegen.bankingapp.models.User;

@Component
public class TransactionExecutePolicy {

    public void enforceTransactionExecutePolicy(Account sender, Account receiver, long amountInCents, User initiator) {
        enforceAccountsNotClosed(sender, receiver);
        enforceInitiatorCanTransferFromSender(initiator, sender);
        enforceSavingsTransferRule(initiator, sender, receiver);
        enforceEmployeeCheckingOnly(initiator, sender, receiver);
        enforceAbsoluteLimit(sender, amountInCents);
    }

    public void enforceAccountsNotClosed(Account sender, Account receiver) {
        if (Boolean.TRUE.equals(sender.getClosed()) || Boolean.TRUE.equals(receiver.getClosed())) {
            throw new BadRequestException("Account is closed");
        }
    }

    public void enforceInitiatorCanTransferFromSender(User initiator, Account sender) {
        if (initiator.getRole() == User.Role.Employee) {
            return;
        }
        if (sender.getOwner().getUserId() != initiator.getUserId()) {
            throw new BadRequestException("You can only transfer from your own account");
        }
    }

    public void enforceSavingsTransferRule(User initiator, Account sender, Account receiver) {
        if (initiator.getRole() == User.Role.Employee) {
            return;
        }
        if ((sender.getAccountType() == Account.AccountType.Savings || receiver.getAccountType() == Account.AccountType.Savings)
            && receiver.getOwner().getUserId() != initiator.getUserId()) {
            throw new BadRequestException("Savings transfers must stay between your own accounts");
        }
    }

    public void enforceEmployeeCheckingOnly(User initiator, Account sender, Account receiver) {
        if (initiator.getRole() != User.Role.Employee) {
            return;
        }
        if (sender.getAccountType() != Account.AccountType.Checking || receiver.getAccountType() != Account.AccountType.Checking) {
            throw new BadRequestException("Employees can only transfer between checking accounts");
        }
    }

    public void enforceAbsoluteLimit(Account sender, long amountInCents) {
        if (sender.getStoredAmountInCents() - amountInCents < sender.getAbsoluteLimitInCents()) {
            throw new BadRequestException("Transfer would drop balance below absolute limit");
        }
    }
}
