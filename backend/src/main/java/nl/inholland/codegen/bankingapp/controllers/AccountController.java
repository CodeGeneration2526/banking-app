package nl.inholland.codegen.bankingapp.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import nl.inholland.codegen.bankingapp.dtos.*;
import nl.inholland.codegen.bankingapp.exceptions.*;
import nl.inholland.codegen.bankingapp.models.*;
import nl.inholland.codegen.bankingapp.services.*;
import nl.inholland.codegen.bankingapp.mappers.AccountMapper;
import nl.inholland.codegen.bankingapp.utils.GetAuthUser;

@RestController
@RequestMapping("/accounts")
@Tag(name = "Account", description = "Account related endpoints")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;
    private final AccountMapper accountMapper;
    private final GetAuthUser getAuthUser;

    public AccountController(
            AccountService accountService,
            UserService userService,
            AccountMapper accountMapper,
            GetAuthUser getAuthUser
    ) {
		this.accountService = accountService;
		this.userService = userService;
		this.accountMapper = accountMapper;
		this.getAuthUser = getAuthUser;
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

    @PostMapping
    // @PreAuthorize("hasRole('ROLE_Employee')")
    public ResponseEntity<AccountDetailResponse> createAccount(@Valid @RequestBody NewAccountRequest request) {
        User issuer = getAuthUser.getAuthUser().orElseThrow(AuthenticationException::new);
        User accountUser = userService.getUser(request.userId()).orElseThrow(() -> new BadRequestException("userId is invalid"));

        Account account = accountMapper.toModel(request, accountUser);

        Account createdAccount = accountService.createAccount(account, issuer);
        AccountDetailResponse accountDetailResponse = accountMapper.toAccountDetailResponse(createdAccount);

        return ResponseEntity.status(HttpStatus.CREATED).body(accountDetailResponse);
    }
}
