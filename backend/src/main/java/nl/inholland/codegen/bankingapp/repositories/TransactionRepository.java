package nl.inholland.codegen.bankingapp.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import nl.inholland.codegen.bankingapp.models.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findBySenderAccount_Owner_UserIdOrReceiverAccount_Owner_UserId(
            long senderOwnerId,
            long receiverOwnerId,
            Pageable pageable);

    List<Transaction> findBySenderAccount_AccountIdAndTimestampBetween(
            long accountId,
            LocalDateTime start,
            LocalDateTime end);
}
