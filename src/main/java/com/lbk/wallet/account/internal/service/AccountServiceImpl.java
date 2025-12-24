package com.lbk.wallet.account.internal.service;

import com.lbk.wallet.account.api.AccountService;
import com.lbk.wallet.account.api.dto.AccountSummary;
import com.lbk.wallet.account.api.dto.GoalItem;
import com.lbk.wallet.account.api.dto.LoanItem;
import com.lbk.wallet.account.api.dto.PayeeItem;
import com.lbk.wallet.account.internal.persistence.entity.AccountDetailEntity;
import com.lbk.wallet.account.internal.persistence.entity.AccountEntity;
import com.lbk.wallet.account.internal.persistence.repo.AccountBalanceRepository;
import com.lbk.wallet.account.internal.persistence.repo.AccountDetailRepository;
import com.lbk.wallet.account.internal.persistence.repo.AccountFlagRepository;
import com.lbk.wallet.account.internal.persistence.repo.AccountRepository;
import com.lbk.wallet.common.api.dto.PageInfo;
import com.lbk.wallet.common.api.dto.PageRequest;
import com.lbk.wallet.common.api.dto.PaginatedResponse;
import com.lbk.wallet.transaction.api.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private static final String TYPE_GOAL = "GOAL";
    private static final String TYPE_LOAN = "LOAN";

    private final AccountRepository accounts;
    private final AccountBalanceRepository balances;
    private final AccountDetailRepository details;
    private final TransactionService transactionService;
    private final AccountFlagRepository flags;

    AccountServiceImpl(AccountRepository accounts, AccountBalanceRepository balances, AccountDetailRepository details,
                       TransactionService transactionService, AccountFlagRepository flags) {
        this.accounts = accounts;
        this.balances = balances;
        this.details = details;
        this.transactionService = transactionService;
        this.flags = flags;
    }

    @Override
    public List<AccountSummary> listAccounts(String userId) {
        log.debug("Fetching accounts for user: {}", userId);
        long startTime = System.currentTimeMillis();

        Map<String, BigDecimal> balByAcc = getBalancesByUserId(userId);

        Map<String, AccountDetailEntity> detByAcc = new HashMap<>();
        for (var d : details.findByUserId(userId)) {
            detByAcc.put(d.getAccountId(), d);
        }

        var accounts = this.accounts.findByUserId(userId).stream()
                .map(a -> mapToAccountSummary(a, balByAcc, detByAcc))
                .toList();

        long duration = System.currentTimeMillis() - startTime;
        log.info("Retrieved {} accounts for user: {} in {}ms", accounts.size(), userId, duration);
        return accounts;
    }

    @Override
    public PaginatedResponse<AccountSummary> listAccounts(String userId, PageRequest pageRequest) {
        log.debug("Fetching paginated accounts for user: {}, page: {}, limit: {}", userId, pageRequest.page(), pageRequest.limit());

        Page<AccountEntity> accountPage =
                accounts.findByUserId(userId, pageRequest.toPageable());

        Map<String, BigDecimal> accBalance = getBalancesByUserId(userId);

        Map<String, AccountDetailEntity> accDetail = new HashMap<>();
        for (var d : details.findByUserId(userId)) {
            accDetail.put(d.getAccountId(), d);
        }

        log.info("Retrieved {} accounts for user: {} (page {} of {})", accountPage.getContent().size(), userId, pageRequest.page(), accountPage.getTotalPages());
        return PaginatedResponse.fromSpringPage(accountPage.map(a -> mapToAccountSummary(a, accBalance, accDetail)));
    }

    /**
     * Maps AccountEntity to AccountSummary with balance and detail information
     */
    private AccountSummary mapToAccountSummary(
            AccountEntity account,
            Map<String, BigDecimal> balances,
            Map<String, AccountDetailEntity> details
    ) {
        BigDecimal bal = balances.get(account.getAccountId());
        var det = details.get(account.getAccountId());
        double amount = bal == null ? 0.0 : bal.doubleValue();
        String color = det == null ? null : det.getColor();
        String status = det == null ? null : getStatus(det.getProgress());
        return new AccountSummary(
                account.getAccountId(),
                account.getType(),
                account.getCurrency(),
                account.getAccountNumber(),
                account.getIssuer(),
                color,
                amount,
                status
        );
    }

    private String getStatus(Integer progress) {
        if (progress != null) {
            if (progress >= 100) {
                return "COMPLETED";
            } else if (progress > 0) {
                return "IN_PROGRESS";
            } else {
                return "NOT_STARTED";
            }
        }
        return "UNKNOWN";
    }

    @Override
    public List<PayeeItem> listQuickPayees(String userId, int limit) {
        log.debug("Fetching {} quick payees for user: {}", limit, userId);

        Set<String> favorites = new HashSet<>();
        flags.findByUserIdAndFlagType(userId, "FAVORITE")
                .forEach(f -> favorites.add(f.getAccountId()));

        var payees = transactionService.listTransactionSummaries(userId).stream()
                .limit(limit)
                .map(t -> new PayeeItem(
                        t.transactionId(),
                        t.name(),
                        t.image(),
                        favorites.contains(t.transactionId())
                ))
                .toList();

        log.info("Retrieved {} quick payees for user: {}", payees.size(), userId);
        return payees;
    }

    @Override
    public PaginatedResponse<PayeeItem> listQuickPayees(String userId, PageRequest pageRequest) {
        log.debug("Fetching paginated quick payees for user: {}, page: {}, limit: {}", userId, pageRequest.page(), pageRequest.limit());

        Set<String> favorites = new HashSet<>();
        flags.findByUserIdAndFlagType(userId, "FAVORITE")
                .forEach(f -> favorites.add(f.getAccountId()));

        var allPayees = transactionService.listTransactionSummaries(userId);

        int offset = pageRequest.getOffset();
        int limit = pageRequest.limit();

        var pageData = allPayees.stream()
                .skip(offset)
                .limit(limit)
                .map(t -> new PayeeItem(
                        t.transactionId(),
                        t.name(),
                        t.image(),
                        favorites.contains(t.transactionId())
                ))
                .toList();

        var pagination = PageInfo.of(
                pageRequest.page(),
                pageRequest.limit(),
                allPayees.size()
        );

        log.info("Retrieved {} quick payees for user: {} (page {} of {})", pageData.size(), userId, pageRequest.page(), pagination.totalPages());
        return PaginatedResponse.of(pageData, pagination);
    }

    @Override
    public Map<String, BigDecimal> getBalancesByUserId(String userId) {
        log.debug("Fetching account balances for user: {}", userId);

        Map<String, BigDecimal> result = new HashMap<>();
        for (var b : balances.findByUserId(userId)) {
            result.put(b.getAccountId(), b.getAmount());
        }

        log.debug("Retrieved {} account balances for user: {}", result.size(), userId);
        return result;
    }

    @Override
    public PaginatedResponse<GoalItem> listGoalAccounts(String userId, PageRequest pageRequest) {
        log.debug("Fetching paginated GOAL accounts for user: {}, page: {}, limit: {}", userId, pageRequest.page(), pageRequest.limit());

        Page<AccountEntity> goalPage = accounts.findByUserIdAndTypeIgnoreCase(userId, TYPE_GOAL, pageRequest.toPageable());

        Map<String, BigDecimal> balancesByAcc = getBalancesByUserId(userId);
        Map<String, AccountDetailEntity> detailsByAcc = new HashMap<>();
        for (var d : details.findByUserId(userId)) {
            detailsByAcc.put(d.getAccountId(), d);
        }

        var goalItems = goalPage.stream()
                .map(a -> mapToAccountSummary(a, balancesByAcc, detailsByAcc))
                .map(a -> new GoalItem(a.accountId(), a.accountNumber(), a.status(), a.issuer(), a.amount()))
                .toList();

        var pageInfo = PageInfo.of(
                pageRequest.page(),
                pageRequest.limit(),
                goalPage.getTotalElements()
        );

        log.info("Retrieved {} GOAL accounts for user: {} (page {} of {})", goalItems.size(), userId, pageRequest.page(), pageInfo.totalPages());
        return PaginatedResponse.of(goalItems, pageInfo);
    }

    @Override
    public PaginatedResponse<LoanItem> listLoanAccounts(String userId, PageRequest pageRequest) {
        log.debug("Fetching paginated LOAN accounts for user: {}, page: {}, limit: {}", userId, pageRequest.page(), pageRequest.limit());

        Page<AccountEntity> loanPage = accounts.findByUserIdAndTypeIgnoreCase(userId, TYPE_LOAN, pageRequest.toPageable());

        Map<String, BigDecimal> balancesByAcc = getBalancesByUserId(userId);
        Map<String, AccountDetailEntity> detailsByAcc = new HashMap<>();
        for (var d : details.findByUserId(userId)) {
            detailsByAcc.put(d.getAccountId(), d);
        }

        var loanItems = loanPage.stream()
                .map(a -> mapToAccountSummary(a, balancesByAcc, detailsByAcc))
                .map(a -> new LoanItem(a.accountId(), a.accountNumber(), a.status(), a.amount()))
                .toList();

        var pageInfo = PageInfo.of(
                pageRequest.page(),
                pageRequest.limit(),
                loanPage.getTotalElements()
        );

        log.info("Retrieved {} LOAN accounts for user: {} (page {} of {})", loanItems.size(), userId, pageRequest.page(), pageInfo.totalPages());
        return PaginatedResponse.of(loanItems, pageInfo);
    }
}
