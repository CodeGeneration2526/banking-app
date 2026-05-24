package nl.inholland.codegen.bankingapp.services;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nl.inholland.codegen.bankingapp.dtos.TransactionRequest;
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
    public Transaction executeTransaction(TransactionRequest req, User initiator) {
        //TODO: Migrate error checking to make use of policies after PR merge
        Account sender = accountRepository.findByIban(req.fromIban())
            .orElseThrow(() -> new NotFoundException("From account not found"));
        Account receiver = accountRepository.findByIban(req.toIban())
            .orElseThrow(() -> new NotFoundException("To account not found"));

        if (Boolean.TRUE.equals(sender.getClosed()) || Boolean.TRUE.equals(receiver.getClosed())) {
            throw new BadRequestException("Account is closed");
        }

        validateTransfer(initiator, sender, receiver);

        if (sender.getStoredAmountInCents() - req.amountInCents() < sender.getAbsoluteLimitInCents()) {
            throw new BadRequestException("Transfer would drop balance below absolute limit");
        }

        dailyLimitCheck(sender, req.amountInCents());

        sender.setStoredAmountInCents(sender.getStoredAmountInCents() - req.amountInCents());
        receiver.setStoredAmountInCents(receiver.getStoredAmountInCents() + req.amountInCents());

        Transaction t = new Transaction();
        t.setSenderAccount(sender);
        t.setReceiverAccount(receiver);
        t.setAmountInCents(req.amountInCents());
        t.setTimestamp(LocalDateTime.now());
        t.setInitiatedBy(initiator);
        return transactionRepository.save(t);
    }

    public Page<Transaction> getTransactions(User authUser, Long customerIdFilter, LocalDate dateFrom, LocalDate dateTo, String iban, Pageable pageable) {
        boolean isEmployee = authUser.getRole() == User.Role.Employee;

        Specification<Transaction> scope = null;
        if (!isEmployee) {
            scope = TransactionSpecifications.ownerIs(authUser.getUserId());
        } else if (customerIdFilter != null) {
            scope = TransactionSpecifications.ownerIs(customerIdFilter);
        }

        Specification<Transaction> spec = Specification.allOf(
            scope,
            dateFrom != null ? TransactionSpecifications.timestampOnOrAfter(dateFrom.atStartOfDay()) : null,
            dateTo   != null ? TransactionSpecifications.timestampBefore(dateTo.plusDays(1).atStartOfDay()) : null,
            (iban != null && !iban.isBlank()) ? TransactionSpecifications.involvesIban(iban) : null
        );

        return transactionRepository.findAll(spec, pageable);
    }

    private void dailyLimitCheck(Account sender, long amountInCents) {
        //Grab the beginning of the day and the end of tomorrow and get all transactions from this period
        //Compare this amount to the daily limit and throw an error when exceeding.
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        long usedToday = transactionRepository
            .findBySenderAccount_AccountIdAndTimestampBetween(sender.getAccountId(), startOfDay, startOfNextDay)
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
