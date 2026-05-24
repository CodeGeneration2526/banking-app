package nl.inholland.codegen.bankingapp.controllers;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import nl.inholland.codegen.bankingapp.mappers.TransactionMapper;
import nl.inholland.codegen.bankingapp.models.Transaction;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.services.TransactionService;
import nl.inholland.codegen.bankingapp.dtos.TransactionRequest;
import nl.inholland.codegen.bankingapp.dtos.TransactionResponse;
import nl.inholland.codegen.bankingapp.exceptions.AuthenticationException;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transaction", description = "Transaction endpoints")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    public TransactionController(TransactionService transactionService, TransactionMapper transactionMapper) {
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }

    @PostMapping
    @Operation(summary = "Execute a transaction", description = "Transfers funds between two accounts")
    public ResponseEntity<TransactionResponse> executeTransaction(@Valid @RequestBody TransactionRequest request) {
        User initiator = getAuthUser().orElseThrow(() -> new AuthenticationException());

        Transaction transaction = transactionService.executeTransaction(request, initiator);

        return ResponseEntity.status(HttpStatus.CREATED).body(transactionMapper.toTransactionResponse(transaction));
    }

    @GetMapping
    @Operation(summary = "List transactions", description = "Customer sees own; employee sees all or filtered by ?customerId=")
    public ResponseEntity<PagedModel<TransactionResponse>> getTransactions(
            @RequestParam(required = false) Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        User authUser = getAuthUser().orElseThrow(() -> new AuthenticationException());

        Page<TransactionResponse> response = transactionService
            .getTransactions(authUser, customerId, PageRequest.of(page, pageSize))
            .map(transactionMapper::toTransactionResponse);

        return ResponseEntity.ok(new PagedModel<>(response));
    }

    private Optional<User> getAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return Optional.empty();
        }

        User user = (User)authentication.getPrincipal();
        return Optional.of(user);
    }
}
