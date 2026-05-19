package nl.inholland.codegen.bankingapp.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import nl.inholland.codegen.bankingapp.models.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    /// Okay, so this is ridiculous lmfao:
    /// findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCaseAndIban
    /// 
    /// Ohhh SQL my beloved
    @Query("""
        SELECT a from Account a
        WHERE LOWER(a.owner.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))
          AND LOWER(a.owner.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))
          AND a.iban = :iban
          AND a.accountType = :accountType
    """)
    Page<Account> search(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("iban") String iban,
            @Param("accountType") Account.AccountType accountType,
            Pageable pageable
    );

    Optional<Account> findByAccountId(long accountId);
}
