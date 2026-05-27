package nl.inholland.codegen.bankingapp.services;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nl.inholland.codegen.bankingapp.dtos.TransactionFilter;
import nl.inholland.codegen.bankingapp.exceptions.BadRequestException;
import nl.inholland.codegen.bankingapp.exceptions.NotFoundException;
import nl.inholland.codegen.bankingapp.models.Account;
import nl.inholland.codegen.bankingapp.models.Transaction;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.policies.TransactionExecutePolicy;
import nl.inholland.codegen.bankingapp.repositories.AccountRepository;
import nl.inholland.codegen.bankingapp.repositories.TransactionRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionExecutePolicy transactionExecutePolicy;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              TransactionExecutePolicy transactionExecutePolicy) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.transactionExecutePolicy = transactionExecutePolicy;
    }

    @Transactional
    public Transaction executeTransaction(long fromAccountNumber, long toAccountNumber, long amountInCents, User initiator) {
        Account sender = accountRepository.findByAccountNumber(fromAccountNumber)
            .orElseThrow(() -> new NotFoundException("From account not found"));
        Account receiver = accountRepository.findByAccountNumber(toAccountNumber)
            .orElseThrow(() -> new NotFoundException("To account not found"));

        transactionExecutePolicy.enforceTransactionExecutePolicy(sender, receiver, amountInCents, initiator);
        //Daily limit check outside of policy due to DB call
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

    public Page<Transaction> getTransactions(User authUser, Long userId, TransactionFilter filter, Pageable pageable) {
        boolean isEmployee = authUser.getRole() == User.Role.Employee;

        Specification<Transaction> scope = null;
        if (!isEmployee) {
            scope = TransactionSpecifications.ownerIs(authUser.getUserId());
        } else if (userId != null) {
            scope = TransactionSpecifications.ownerIs(userId);
        }

        Specification<Transaction> spec = Specification.where(scope).and(filter.toSpecification());
        return transactionRepository.findAll(spec, pageable);
    }

    private void dailyLimitCheck(Account sender, long amountInCents) {
        //Grab the beginning of the day and the end of the day and get all transactions from this period
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
}
