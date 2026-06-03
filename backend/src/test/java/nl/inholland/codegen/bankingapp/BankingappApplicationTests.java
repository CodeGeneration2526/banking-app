package nl.inholland.codegen.bankingapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=test-secret-must-be-at-least-32-bytes-long-xx"
})
class BankingappApplicationTests {

    @Test
    void contextLoads() {
    }

}
