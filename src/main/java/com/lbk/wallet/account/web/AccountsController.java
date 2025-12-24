package com.lbk.wallet.account.web;

import com.lbk.wallet.account.api.AccountService;
import com.lbk.wallet.account.api.dto.AccountSummary;
import com.lbk.wallet.account.api.dto.GoalItem;
import com.lbk.wallet.account.api.dto.LoanItem;
import com.lbk.wallet.account.api.dto.PayeeItem;
import com.lbk.wallet.common.api.dto.PageRequest;
import com.lbk.wallet.common.api.dto.PaginatedResponse;
import com.lbk.wallet.transaction.api.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "ฺBearer Token")
@Tag(name = "Account", description = "Endpoints for retrieving user account data")
@RestController
@RequestMapping("/v1/accounts")
@Validated
public class AccountsController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountsController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @Operation(summary = "Get Account Summary data", description = "Retrieve a list of account summaries for the authenticated user")
    @GetMapping
    public PaginatedResponse<AccountSummary> list(
            Authentication auth,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer limit
    ) {
        return accountService.listAccounts(auth.getName(), new PageRequest(page, limit));
    }

    @Operation(summary = "Get Account Transactions", description = "Retrieve a paginated list of transactions for a specific account")
    @GetMapping("/{accountId}/transactions")
    public TransactionService.TransactionsPage transactions(
            Authentication auth,
            @PathVariable String accountId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        return transactionService.listTransactions(auth.getName(), accountId, cursor, limit);
    }

    @Operation(summary = "Get Goals Account", description = "Retrieve a list of goal accounts for the authenticated user")
    @GetMapping("/goals")
    public PaginatedResponse<GoalItem> getGoalsAccount(
            Authentication auth,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer limit
    ) {
        return accountService.listGoalAccounts(auth.getName(), new PageRequest(page, limit));
    }

    @Operation(summary = "Get Loans Account", description = "Retrieve a list of loans accounts for the authenticated user")
    @GetMapping("/loans")
    public PaginatedResponse<LoanItem> getLoansAccount(
            Authentication auth,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer limit
    ) {
        return accountService.listLoanAccounts(auth.getName(), new PageRequest(page, limit));
    }

    @Operation(summary = "Get Payees", description = "Retrieve a list of payee favorites for the authenticated user")
    @GetMapping("/payees")
    public PaginatedResponse<PayeeItem> listPayees(
            Authentication auth,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) Integer limit
    ) {
        return accountService.listQuickPayees(auth.getName(), new PageRequest(page, limit));
    }
}
