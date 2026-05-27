package nl.inholland.codegen.bankingapp.services;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nl.inholland.codegen.bankingapp.exceptions.BadRequestException;
import nl.inholland.codegen.bankingapp.exceptions.NotFoundException;
import nl.inholland.codegen.bankingapp.models.Account;
import nl.inholland.codegen.bankingapp.models.Transaction;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.repositories.AccountRepository;
import nl.inholland.codegen.bankingapp.repositories.TransactionRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Transaction executeTransaction(long fromAccountNumber, long toAccountNumber, long amountInCents, User initiator) {
        //TODO: Migrate error checking to make use of policies after PR merge
        Account sender = accountRepository.findByAccountNumber(fromAccountNumber)
            .orElseThrow(() -> new NotFoundException("From account not found"));
        Account receiver = accountRepository.findByAccountNumber(toAccountNumber)
            .orElseThrow(() -> new NotFoundException("To account not found"));

        if (Boolean.TRUE.equals(sender.getClosed()) || Boolean.TRUE.equals(receiver.getClosed())) {
            throw new BadRequestException("Account is closed");
        }

        validateTransfer(initiator, sender, receiver);

        if (sender.getStoredAmountInCents() - amountInCents < sender.getAbsoluteLimitInCents()) {
            throw new BadRequestException("Transfer would drop balance below absolute limit");
        }

        dailyLimitCheck(sender, amountInCents);

        sender.setStoredAmountInCents(sender.getStoredAmountInCents() - amountInCents);
        receiver.setStoredAmountInCents(receiver.getStoredAmountInCents() + amountInCents);

        Transaction t = new Transaction();
        t.setSenderAccount(sender);
        t.setReceiverAccount(receiver);
        t.setAmountInCents(amountInCents);
        t.setTimestamp(LocalDateTime.now());
        t.setInitiatedBy(initiator);
        return transactionRepository.save(t);
    }

    public Page<Transaction> getTransactions(User authUser, Long userId, LocalDate dateFrom, LocalDate dateTo, Long accountNumber, Long amountInCents, TransactionSpecifications.AmountFilter amountFilter, Pageable pageable) {
        boolean isEmployee = authUser.getRole() == User.Role.Employee;

        Specification<Transaction> scope = null;
        if (!isEmployee) {
            scope = TransactionSpecifications.ownerIs(authUser.getUserId());
        } else if (userId != null) {
            scope = TransactionSpecifications.ownerIs(userId);
        }

        Specification<Transaction> spec = Specification.where(scope);
        if (dateFrom != null) spec = spec.and(TransactionSpecifications.timestampOnOrAfter(dateFrom.atStartOfDay()));
        if (dateTo   != null) spec = spec.and(TransactionSpecifications.timestampBefore(dateTo.plusDays(1).atStartOfDay()));
        if (accountNumber != null) spec = spec.and(TransactionSpecifications.involvesAccountNumber(accountNumber));
        if (amountInCents != null) spec = spec.and(TransactionSpecifications.amountCompare(amountInCents, amountFilter));

        return transactionRepository.findAll(spec, pageable);
    }

    private void dailyLimitCheck(Account sender, long amountInCents) {
        //Grab the beginning of the day and the end of tomorrow and get all transactions from this period
        //Compare this amount to the daily limit and throw an error when exceeding.
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        long usedToday = transactionRepository
            .findBySenderAccount_AccountIdAndTimestampAfterAndTimestampBefore(sender.getAccountId(), startOfDay, startOfNextDay)
            .stream().mapToLong(Transaction::getAmountInCents).sum();

        if (usedToday + amountInCents > sender.getDailyLimitInCents()) {
            throw new BadRequestException("Daily transfer limit exceeded");
        }
    }

    private void validateTransfer(User initiator, Account sender, Account receiver) {
        //Ignore validations if the user is an employee
        if (initiator.getRole() == User.Role.Employee) {
            return;
        }

        long initiatorId = initiator.getUserId();
        if (sender.getOwner().getUserId() != initiatorId) {
            throw new BadRequestException("You can only transfer from your own account");
        }

        if ((sender.getAccountType() == Account.AccountType.Savings || receiver.getAccountType() == Account.AccountType.Savings)
            && receiver.getOwner().getUserId() != initiatorId) {
            throw new BadRequestException("Savings transfers must stay between your own accounts");
        }
    }
}
