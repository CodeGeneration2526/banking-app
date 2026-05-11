package nl.inholland.codegen.bankingapp.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import nl.inholland.codegen.bankingapp.dtos.AccountDetailDto;
import nl.inholland.codegen.bankingapp.dtos.AccountSummaryDto;
import nl.inholland.codegen.bankingapp.dtos.CustomerLookupDto;

@RestController
@RequestMapping("/customers")
@Validated
@Tag(name = "Customer", description = "Customer account endpoints")
public class CustomerController {

    @GetMapping("/accounts")
    @Operation(summary = "List my accounts", description = "Returns all accounts for the authenticated customer")
    public ResponseEntity<List<AccountSummaryDto>> listAllAccounts() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/accounts/{accountId}")
    @Operation(summary = "Get account details", description = "Returns details for a specific customer account")
    public ResponseEntity<AccountDetailDto> getAccountInfo(
            @Parameter(description = "Account ID") @PathVariable @Min(1) long accountId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search customers by IBAN or name",
               description = "Lookup customer IBANs by providing an IBAN, first name, or last name. At least one parameter is required.")
    public ResponseEntity<List<CustomerLookupDto>> searchCustomers(
            @Parameter(description = "IBAN to search") @RequestParam(required = false) String iban,
            @Parameter(description = "Customer first name") @RequestParam(required = false) String firstName,
            @Parameter(description = "Customer last name") @RequestParam(required = false) String lastName) {

        if (iban == null && firstName == null && lastName == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
