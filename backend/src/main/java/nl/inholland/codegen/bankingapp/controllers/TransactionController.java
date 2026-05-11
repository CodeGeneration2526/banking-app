package nl.inholland.codegen.bankingapp.controllers;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import nl.inholland.codegen.bankingapp.dtos.TransactionDto;
import nl.inholland.codegen.bankingapp.utils.PaginatedList;

@RestController
@RequestMapping("/transactions")
@Validated
@Tag(name = "Transaction", description = "Transaction endpoints")
public class TransactionController {

    private static final String IBAN_PATTERN = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$";

    @PostMapping
    @Operation(summary = "Issue a transaction", description = "Transfer funds between two checking accounts")
    public ResponseEntity<TransactionDto> issueTransaction(
            @Valid @RequestBody TransactionRequestBody body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping
    @Operation(summary = "List transactions", description = "Returns a paginated list of transactions for the authenticated customer")
    public ResponseEntity<PaginatedList<TransactionDto>> listTransactions(
            @Parameter(description = "Filter by source IBAN")
            @RequestParam(required = false)
            @Pattern(regexp = IBAN_PATTERN, message = "Invalid IBAN format")
            String fromIban,

            @Parameter(description = "Filter by destination IBAN")
            @RequestParam(required = false)
            @Pattern(regexp = IBAN_PATTERN, message = "Invalid IBAN format")
            String toIban,

            @Parameter(description = "Start of date range (inclusive), format: yyyy-MM-dd")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateRangeStart,

            @Parameter(description = "End of date range (inclusive), format: yyyy-MM-dd")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateRangeEnd,

            @Parameter(description = "Amount filter in cents")
            @RequestParam(required = false)
            @Min(value = 0, message = "Amount must be non-negative")
            Integer amountCents,

            @Parameter(description = "Comparison operator for amount: GreaterThan, EqualTo, LessThan")
            @RequestParam(required = false)
            AmountFilter amountFilter,

            @Parameter(description = "Account type filter: Current, Savings, Both")
            @RequestParam(defaultValue = "Both")
            AccountType accountType,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be 0 or greater")
            int page,

            @Parameter(description = "Number of results per page (1–100)")
            @RequestParam(defaultValue = "16")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size must not exceed 100")
            int pageSize,

            @Parameter(description = "Sort order by timestamp: Ascending or Descending")
            @RequestParam(defaultValue = "Descending")
            TimestampSort timestampSort) {

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }


    public record TransactionRequestBody(
        @Pattern(regexp = IBAN_PATTERN, message = "Invalid source IBAN format")
        String fromIban,

        @Pattern(regexp = IBAN_PATTERN, message = "Invalid destination IBAN format")
        String toIban,

        @Min(value = 1, message = "Transfer amount must be at least 1 cent")
        int amountCents,

        String description
    ) {}


    public enum AmountFilter { GreaterThan, EqualTo, LessThan }
    public enum TimestampSort { Descending, Ascending }
    public enum AccountType { Current, Savings, Both }
}
