package nl.inholland.codegen.bankingapp.controllers;

import java.time.LocalDate;

import nl.inholland.codegen.bankingapp.utils.GetAuthUser;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import nl.inholland.codegen.bankingapp.dtos.TransactionRequest;
import nl.inholland.codegen.bankingapp.dtos.TransactionResponse;
import nl.inholland.codegen.bankingapp.exceptions.AuthenticationException;
import nl.inholland.codegen.bankingapp.mappers.TransactionMapper;
import nl.inholland.codegen.bankingapp.models.Transaction;
import nl.inholland.codegen.bankingapp.models.User;
import nl.inholland.codegen.bankingapp.services.TransactionService;
import nl.inholland.codegen.bankingapp.services.TransactionSpecifications;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transaction", description = "Transaction endpoints")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;
    private final GetAuthUser getAuthUser;

    public TransactionController(TransactionService transactionService, TransactionMapper transactionMapper, GetAuthUser getAuthUser) {
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
        this.getAuthUser = getAuthUser;
    }

    @PostMapping
    @Operation(summary = "Execute a transaction", description = "Transfers funds between two accounts")
    public ResponseEntity<TransactionResponse> executeTransaction(@Valid @RequestBody TransactionRequest request) {
        User initiator = getAuthUser.getAuthUser().orElseThrow(() -> new AuthenticationException());

        Transaction transaction = transactionService.executeTransaction(request, initiator);

        return ResponseEntity.status(HttpStatus.CREATED).body(transactionMapper.toTransactionResponse(transaction));
    }

    @GetMapping
    @Operation(summary = "List transactions",
               description = "Customer sees own; employee sees all or filtered by userId. " +
                             "Optional filters: dateFrom, dateTo, iban, amountInCents (with amountFilter LessThan|EqualTo|GreaterThan, default EqualTo). " +
                             "Sort via ?sort=field,dir (default timestamp,desc).")
    public ResponseEntity<PagedModel<TransactionResponse>> getTransactions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) Long amountInCents,
            @RequestParam(defaultValue = "EqualTo") TransactionSpecifications.AmountFilter amountFilter,
            @ParameterObject @PageableDefault(size = 10, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        User authUser = getAuthUser.getAuthUser().orElseThrow(() -> new AuthenticationException());

        Page<TransactionResponse> response = transactionService
            .getTransactions(authUser, userId, dateFrom, dateTo, iban, amountInCents, amountFilter, pageable)
            .map(transactionMapper::toTransactionResponse);

        return ResponseEntity.ok(new PagedModel<>(response));
    }

}
