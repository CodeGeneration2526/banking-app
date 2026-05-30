package nl.inholland.codegen.bankingapp.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import nl.inholland.codegen.bankingapp.models.Account;

public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    Optional<Account> findByAccountId(long accountId);
    Optional<Account> findByAccountNumber(long accountNumber);
}
