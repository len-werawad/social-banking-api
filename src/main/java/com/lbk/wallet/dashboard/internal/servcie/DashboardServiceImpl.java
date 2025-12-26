package com.lbk.wallet.dashboard.internal.servcie;

import com.lbk.wallet.account.api.AccountService;
import com.lbk.wallet.account.api.dto.AccountSummary;
import com.lbk.wallet.account.api.dto.GoalItem;
import com.lbk.wallet.account.api.dto.LoanItem;
import com.lbk.wallet.account.api.dto.PayeeItem;
import com.lbk.wallet.common.api.dto.PageRequest;
import com.lbk.wallet.customer.api.CustomerService;
import com.lbk.wallet.dashboard.web.dto.DashboardResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private static final int EXECUTOR_POOL_SIZE = 3;
    private static final int QUICK_PAYEES_LIMIT = 10;
    private static final int GOALS_PAGE_SIZE = 10;
    private static final int LOANS_PAGE_SIZE = 10;
    private static final String PRIMARY_ACCOUNT_TYPE = "SAVING";

    private final CustomerService customerService;
    private final AccountService accountsService;
    private final DelegatingSecurityContextExecutorService executorService;

    public DashboardServiceImpl(CustomerService customerService,
                                AccountService accounts) {
        this.customerService = customerService;
        this.accountsService = accounts;
        this.executorService = new DelegatingSecurityContextExecutorService(
                Executors.newFixedThreadPool(EXECUTOR_POOL_SIZE)
        );
    }

    @Cacheable(value = "dashboardData", key = "#userId")
    public DashboardResponse getDashboard(String userId) {
        log.info("Fetching dashboard data for user: {}", userId);
        long startTime = System.currentTimeMillis();

        CompletableFuture<String> greetingFuture = CompletableFuture.supplyAsync(() -> {
            log.debug("Fetching greeting for user: {}", userId);
            return customerService.getGreeting(userId);
        }, executorService);

        CompletableFuture<List<AccountSummary>> accountsFuture =
                CompletableFuture.supplyAsync(() -> {
                    log.debug("Fetching accounts for user: {}", userId);
                    return accountsService.listAccounts(userId);
                }, executorService);

        CompletableFuture<List<PayeeItem>> payeesFuture =
                CompletableFuture.supplyAsync(() -> {
                    log.debug("Fetching quick payees for user: {}", userId);
                    return accountsService.listQuickPayees(userId, QUICK_PAYEES_LIMIT);
                }, executorService);

        CompletableFuture<List<GoalItem>> goalsFuture =
                CompletableFuture.supplyAsync(() -> {
                    log.debug("Fetching goal accounts for user: {}", userId);
                    return accountsService.listGoalAccounts(userId, new PageRequest(1, GOALS_PAGE_SIZE)).data();
                }, executorService);

        CompletableFuture<List<LoanItem>> loansFuture =
                CompletableFuture.supplyAsync(() -> {
                    log.debug("Fetching loan accounts for user: {}", userId);
                    return accountsService.listLoanAccounts(userId, new PageRequest(1, LOANS_PAGE_SIZE)).data();
                }, executorService);

        CompletableFuture<Void> all = CompletableFuture.allOf(
                greetingFuture, accountsFuture, payeesFuture, goalsFuture, loansFuture
        );

        all.join();

        String greeting = greetingFuture.join();
        var accountList = accountsFuture.join();
        var quickPayees = payeesFuture.join();
        var goalItems = goalsFuture.join();
        var loanItems = loansFuture.join();

        var primary = accountList.stream()
                .filter(a -> PRIMARY_ACCOUNT_TYPE.equalsIgnoreCase(a.type()))
                .findFirst()
                .orElse(accountList.isEmpty() ? null : accountList.getFirst());

        var goals = goalItems.stream()
                .map(g -> new DashboardResponse.GoalCard(g.goalId(), g.name(), g.status(), g.amount()))
                .toList();

        var loans = loanItems.stream()
                .map(l -> new DashboardResponse.LoanCard(l.loanId(), l.name(), l.status(), l.outstandingAmount()))
                .toList();

        long duration = System.currentTimeMillis() - startTime;
        log.info("Dashboard data retrieved for user: {} in {}ms - {} accounts, {} payees, {} goals, {} loans",
                userId, duration, accountList.size(), quickPayees.size(), goals.size(), loans.size());

        return new DashboardResponse(greeting, primary, accountList, quickPayees, goals, loans);
    }
}
