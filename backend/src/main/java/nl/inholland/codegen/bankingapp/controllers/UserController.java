package nl.inholland.codegen.bankingapp.controllers;

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
    @GetMapping("/search")
    @Operation(summary = "Search customers", description = "Lookup IBANs by customer name or IBAN.")
    public ResponseEntity<PaginatedList<CustomerLookupResponse>> searchCustomers(
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/approvals")
    @Operation(summary = "Get accounts pending approval", description = "Returns all user accounts with the status PENDING.")
    public ResponseEntity<PaginatedList<ApprovalResponse>> getApprovalAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("/approvals")
    @Operation(summary = "Approve customer and create accounts", description = "Creates a checking and savings account for the given customer")
    public ResponseEntity<Void> createAccounts(@RequestBody CreateAccountRequest request) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
