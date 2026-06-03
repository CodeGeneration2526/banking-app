package nl.inholland.codegen.bankingapp.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import nl.inholland.codegen.bankingapp.models.Transaction;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findBySenderAccount_AccountIdAndTimestampAfterAndTimestampBefore(
            long accountId,
            LocalDateTime start,
            LocalDateTime end);
}
