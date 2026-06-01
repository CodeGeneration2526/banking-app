package nl.inholland.codegen.bankingapp.controllers;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
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
            @RequestParam(required = false) Account.AccountType accountType,
            @RequestParam(required = false) Long ownerUserId,
            @ParameterObject @PageableDefault(size = 10, sort = "accountId", direction = Sort.Direction.DESC) Pageable pageable
    )
    {
        User user = getAuthUser.getAuthUser().orElseThrow(AuthenticationException::new);

        Specification<Account> spec = Specification
            .where(AccountSpecifications.visibleTo(user))
            .and(AccountSpecifications.firstNameContains(firstName))
            .and(AccountSpecifications.lastNameContains(lastName))
            .and(AccountSpecifications.ibanEquals(iban))
            .and(AccountSpecifications.accountTypeEquals(accountType))
            .and(AccountSpecifications.ownerUserId(ownerUserId));

        Page<AccountSummaryResponse> resp = accountService.getAllAccounts(spec, pageable).map(accountMapper::toAccountSummaryResponse);
        return ResponseEntity.ok(new PagedModel<>(resp));
    }

    @GetMapping("{accountId}")
    @Operation(summary = "Get account details", description = "Returns details for a single customer account.")
    public ResponseEntity<AccountDetailResponse> getAccountInfo(@PathVariable long accountId) {
        User user = getAuthUser.getAuthUser().orElseThrow(AuthenticationException::new);

        Account account = accountService.getAccountInfo(accountId)
            .orElseThrow(() -> new NotFoundException("Account ID does not exist"));

        if (user.getRole() != User.Role.Employee && account.getOwner().getUserId() != user.getUserId()) {
            throw new AuthenticationException();
        }

        return ResponseEntity.ok(accountMapper.toAccountDetailResponse(account));
    }

    @PatchMapping("{accountId}")
    @Operation(summary = "Update specific savings or checking account", description = "Updates the absolute limit, daily limit, or closed status of an account.")
    @PreAuthorize("hasRole('Employee')")
    public ResponseEntity<AccountDetailResponse> updateAccount(
            @PathVariable long id,
            @RequestBody UpdateAccountRequest request) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @DeleteMapping("{accountId}")
    @Operation(summary = "Close an account", description = "Employee can close a specific account from an user.")
    public ResponseEntity<ApiResponse> closeAccount(@PathVariable long accountId) {
        User user = getAuthUser.getAuthUser().orElseThrow(AuthenticationException::new);

        Account account = accountService.getAccountInfo(accountId)
            .orElseThrow(() -> new NotFoundException("Account ID does not exist"));

        if (user.getRole() != User.Role.Employee && account.getOwner().getUserId() != user.getUserId()) {
            throw new AuthenticationException();
        }

        accountService.closeAccount(accountId);

        ApiResponse resp = new ApiResponse("Account with the id " + accountId + " has been closed");
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    @Operation(summary = "Approve customer and create accounts", description = "Approves the given pending customer and creates one checking and one savings account.")
    @PreAuthorize("hasRole('Employee')")
    public ResponseEntity<ApiResponse> createAccount(@Valid @RequestBody NewAccountRequest request) {
        User issuer = getAuthUser.getAuthUser().orElseThrow(() -> new AuthenticationException());
        User accountUser = userService.getUser(request.userId()).orElseThrow(() -> new BadRequestException("userId is invalid"));

        accountService.approveAndCreateAccounts(
            accountUser, issuer, request.absoluteLimitInCents(), request.dailyLimitInCents());

        // The created accounts aren't returned at the moment, can be changed later
        ApiResponse resp = new ApiResponse("User successfully approved and accounts created.");
        return ResponseEntity.ok(resp);
    }
}
