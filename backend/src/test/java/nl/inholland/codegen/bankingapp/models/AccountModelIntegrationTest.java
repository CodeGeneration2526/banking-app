package nl.inholland.codegen.bankingapp.models;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import nl.inholland.codegen.bankingapp.repositories.AccountRepository;

@SpringBootTest
@Transactional
class AccountModelIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private EntityManager entityManager;
    

    @Test
    void testAccountCreation() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void testAccountOwnership() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void testAccountIbanUnique() {
        throw new IllegalStateException("Unimplemented");
    }
}
