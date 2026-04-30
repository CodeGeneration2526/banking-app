package nl.inholland.codegen.bankingapp.controllers;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import nl.inholland.codegen.bankingapp.dtos.TransactionDto;
import nl.inholland.codegen.bankingapp.utils.PaginatedList;

@RestController
@RequestMapping("/transaction")
@Tag(name = "Transaction", description = "Transaction endpoints")
public class TransactionController {

    @PostMapping
    @Operation(summary = "Issue a transaction", description = "Issues a transaction between two checking accounts")
    public ResponseEntity<Void> issueTransaction(
            @RequestBody String fromIban,
            @RequestBody String toIban) {

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping
    @Operation(summary = "List transactions", description = "Returns a paginated list of transactions for the customer")
    public ResponseEntity<PaginatedList<TransactionDto>> listTransactions(
            @RequestParam(required = false) String fromIban,
            @RequestParam(required = false) String toIban,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateRangeStart,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate dateRangeEnd,
            @RequestParam(required = false) Integer amountCents,
            @RequestParam(required = false) AmountFilter amountFilter,
            @RequestParam(defaultValue = "Both") AccountType accountType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "16") int pageSize,
            @RequestParam(defaultValue = "Descending") TimestampSort timestampSort) {

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public enum AmountFilter {
        GreaterThan,
        EqualTo,
        LessThan,
    }

    public enum TimestampSort {
        Descending,
        Ascending,
    }

    public enum AccountType {
        Current,
        Savings,
        Both,
    }

}
