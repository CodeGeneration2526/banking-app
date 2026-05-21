package nl.inholland.codegen.bankingapp.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void issueTransaction_ReturnsInternalServerError() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }

    @Test
    void listTransactions_ReturnsInternalServerError() throws Exception {
        throw new IllegalStateException("Unimplemented");
    }
}
