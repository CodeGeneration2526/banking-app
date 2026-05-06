package nl.inholland.codegen.bankingapp.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import nl.inholland.codegen.bankingapp.dtos.*;
import nl.inholland.codegen.bankingapp.utils.PaginatedList;

@RestController
@RequestMapping("/accounts")
@Tag(name = "Account", description = "Account related endpoints")
public class AccountController {
    @GetMapping
    @Operation(summary = "List customer savings and checking accounts", description = "Returns all accounts for the authenticated user, or can be used to search for other users. Employees can view all accounts.")
    public ResponseEntity<PaginatedList<AccountSummaryResponse>> listAllAccounts(
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("{accountId}")
    @Operation(summary = "Get account details", description = "Returns details for a single customer account.")
    public ResponseEntity<AccountDetailResponse> getAccountInfo(
            @PathVariable long accountId) {
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PatchMapping("{id}")
    @Operation(summary = "Update specific savings or checking account", description = "Updates the absolute limit, daily limit, or closed status of an account")
    public ResponseEntity<AccountDetailResponse> updateAccount(
            @PathVariable long id,
            @RequestBody UpdateAccountRequest request) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
