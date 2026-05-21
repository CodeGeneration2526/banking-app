package nl.inholland.codegen.bankingapp.models;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import nl.inholland.codegen.bankingapp.repositories.UserRepository;

@SpringBootTest
@Transactional
class UserModelIntegrationTest {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private UserRepository userRepository;

    @Test
    void testUserCreation() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void testUserApprovalRelationship() {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void testUserEmailUnique() {
        throw new IllegalStateException("Unimplemented");
    }
}
