package nl.inholland.codegen.bankingapp.controllers;

import java.util.List;

import nl.inholland.codegen.bankingapp.dtos.*;
import nl.inholland.codegen.bankingapp.utils.PaginatedList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "User", description = "User account and transaction endpoints")
public class UserController {

    @GetMapping("/accounts")
    @Operation(summary = "List customer accounts", description = "Returns all accounts for the authenticated customer.")
    public ResponseEntity<List<AccountSummaryDto>> listAllAccounts() {
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/accounts/{accountId}")
    @Operation(summary = "Get account details", description = "Returns details for a single customer account.")
    public ResponseEntity<AccountDetailDto> getAccountInfo(
            @PathVariable long accountId) {
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/search")
    @Operation(summary = "Search customers", description = "Lookup IBANs by customer name or IBAN.")
    public ResponseEntity<List<CustomerLookupDto>> searchCustomers(
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName) {
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/approvals")
    @Operation(summary = "Get accounts pending approval", description = "Returns all user accounts with the status PENDING.")
    public ResponseEntity<PaginatedList<ApprovalDto>> getApprovalAccounts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue =  "10") int pageSize) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("/approvals")
    @Operation(summary = "Approve customer and create accounts", description = "Creates a checking and savings account for the given customer")
    public ResponseEntity<Void> createAccounts(@RequestBody CreateAccountRequestDto request) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PatchMapping("/accounts/{id}")
    @Operation(summary = "Update specific account", description = "Updates the absolute limit, daily limit, or closed status of an account")
    public ResponseEntity<AccountDetailDto> updateAccount(
        @PathVariable long id,
        @RequestBody UpdateAccountRequestDto request) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
