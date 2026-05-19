package nl.inholland.codegen.bankingapp.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import nl.inholland.codegen.bankingapp.dtos.*;
import nl.inholland.codegen.bankingapp.exceptions.NotFoundException;
import nl.inholland.codegen.bankingapp.mappers.AccountMapper;
import nl.inholland.codegen.bankingapp.services.AccountService;

@RestController
@RequestMapping("/accounts")
@Tag(name = "Account", description = "Account related endpoints")
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public AccountController(AccountService accountService, AccountMapper accountMapper) {
		this.accountService = accountService;
		this.accountMapper = accountMapper;
	}


	@GetMapping
    @Operation(
        summary = "List customer savings and checking accounts",
        description = "Returns all accounts for the authenticated user, or can be used to search for other users. Employees can view all accounts.")
    public ResponseEntity<PagedModel<AccountSummaryResponse>> listAllAccounts(
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize)
    {
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<AccountSummaryResponse> resp = accountService.searchCheckingAccounts(firstName, lastName, iban, pageable)
            .map(accountMapper::toAccountSummaryResponse);

        return ResponseEntity.ok(new PagedModel<>(resp));
    }

    @GetMapping("{accountId}")
    @Operation(summary = "Get account details", description = "Returns details for a single customer account.")
    public ResponseEntity<AccountDetailResponse> getAccountInfo(@PathVariable long accountId) {
        AccountDetailResponse account = accountService.getAccountInfo(accountId)
            .map(accountMapper::toAccountDetailResponse)
            .orElseThrow(() -> new NotFoundException("Account ID does not exist"));

        return ResponseEntity.ok(account);
    }

    @PatchMapping("{accountId}")
    @Operation(summary = "Update specific savings or checking account", description = "Updates the absolute limit, daily limit, or closed status of an account.")
    public ResponseEntity<AccountDetailResponse> updateAccount(
            @PathVariable long id,
            @RequestBody UpdateAccountRequest request) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @DeleteMapping("{accountId}")
    @Operation(summary = "Close an account", description = "Employee can close a specific account from an user.")
    public ResponseEntity<ApiResponse> closeAccount(@PathVariable long accountId) {
        accountService.closeAccount(accountId);

        ApiResponse resp = new ApiResponse("Account with the id " + accountId + " has been closed");
        return ResponseEntity.ok(resp);
    }
}
