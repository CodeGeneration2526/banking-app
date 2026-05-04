package nl.inholland.codegen.bankingapp.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import nl.inholland.codegen.bankingapp.dtos.AccountDetailDto;
import nl.inholland.codegen.bankingapp.dtos.ApprovalDto;
import nl.inholland.codegen.bankingapp.dtos.CreateAccountRequestDto;
import nl.inholland.codegen.bankingapp.dtos.TransactionDto;
import nl.inholland.codegen.bankingapp.dtos.TransferRequestDto;
import nl.inholland.codegen.bankingapp.dtos.UpdateAccountRequestDto;
import nl.inholland.codegen.bankingapp.utils.PaginatedList;

@RestController
@RequestMapping("/employee")
@Tag(name = "Employee", description = "Employee endpoints")
public class EmployeeController {
    public EmployeeController() {}

    @GetMapping("/approvals")
    @Operation(summary = "Get pending approvals", description = "Returns all customers waiting to be approved")
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

    @PostMapping("/transactions")
    @Operation(summary = "Transfer funds between two accounts", description = "Employee-initiated transfer between two customer accounts")
    public ResponseEntity<TransactionDto> transfer(@RequestBody TransferRequestDto request) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
