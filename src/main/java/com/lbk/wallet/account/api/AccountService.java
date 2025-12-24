package com.lbk.wallet.account.api;

import com.lbk.wallet.account.api.dto.AccountSummary;
import com.lbk.wallet.account.api.dto.GoalItem;
import com.lbk.wallet.account.api.dto.LoanItem;
import com.lbk.wallet.account.api.dto.PayeeItem;
import com.lbk.wallet.common.api.dto.PageRequest;
import com.lbk.wallet.common.api.dto.PaginatedResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AccountService {
    List<AccountSummary> listAccounts(String userId);

    PaginatedResponse<AccountSummary> listAccounts(String userId, PageRequest pageRequest);

    PaginatedResponse<GoalItem> listGoalAccounts(String userId, PageRequest pageRequest);

    PaginatedResponse<LoanItem> listLoanAccounts(String userId, PageRequest pageRequest);

    List<PayeeItem> listQuickPayees(String userId, int limit);

    PaginatedResponse<PayeeItem> listQuickPayees(String userId, PageRequest pageRequest);

    Map<String, BigDecimal> getBalancesByUserId(String userId);
}
