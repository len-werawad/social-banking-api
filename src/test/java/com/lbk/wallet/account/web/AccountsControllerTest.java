package com.lbk.wallet.account.web;

import com.lbk.wallet.account.api.AccountService;
import com.lbk.wallet.account.api.dto.AccountSummary;
import com.lbk.wallet.account.api.dto.PayeeItem;
import com.lbk.wallet.common.api.JwtService;
import com.lbk.wallet.common.api.dto.PageInfo;
import com.lbk.wallet.common.api.dto.PageRequest;
import com.lbk.wallet.common.api.dto.PaginatedResponse;
import com.lbk.wallet.transaction.api.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccountsController.class)
class AccountsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private TransactionService transactionService;

    @Nested
    @DisplayName("GET /v1/accounts - List Accounts")
    class ListAccountsTests {

        @Test
        @DisplayName("should return paginated list of accounts with default pagination")
        @WithMockUser(username = "u1")
        void list_shouldReturnPaginatedAccounts() throws Exception {
            var paginatedResponse = PaginatedResponse.of(
                    List.of(
                            new AccountSummary("acc-1", "SAVING", "THB", "123-456", "KBank", "#FF5733", 1000.00, "IN_PROGRESS"),
                            new AccountSummary("acc-2", "GOAL", "THB", "222-333", "SCB", "#3357FF", 500.00, "IN_PROGRESS")
                    ),
                    PageInfo.of(1, 20, 2)
            );
            when(accountService.listAccounts("u1", new PageRequest(1, 20))).thenReturn(paginatedResponse);

            mockMvc.perform(get("/v1/accounts"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data.[0].accountId").value("acc-1"))
                    .andExpect(jsonPath("$.data.[0].type").value("SAVING"))
                    .andExpect(jsonPath("$.data.[0].accountNumber").value("123-456"))
                    .andExpect(jsonPath("$.data.[0].issuer").value("KBank"))
                    .andExpect(jsonPath("$.data.[0].amount").value(1000.00))
                    .andExpect(jsonPath("$.data.[1].accountId").value("acc-2"))
                    .andExpect(jsonPath("$.data.[1].type").value("GOAL"))
                    .andExpect(jsonPath("$.pagination").exists())
                    .andExpect(jsonPath("$.pagination.page").value(1))
                    .andExpect(jsonPath("$.pagination.limit").value(20))
                    .andExpect(jsonPath("$.pagination.total").value(2))
                    .andExpect(jsonPath("$.pagination.totalPages").value(1));
        }

        @Test
        @DisplayName("should return empty paginated list when no accounts")
        @WithMockUser(username = "u1")
        void list_shouldReturnEmptyPaginatedList() throws Exception {
            var emptyResponse = PaginatedResponse.of(
                    List.<AccountSummary>of(),
                    PageInfo.of(1, 20, 0)
            );
            when(accountService.listAccounts("u1", new PageRequest(1, 20))).thenReturn(emptyResponse);

            mockMvc.perform(get("/v1/accounts"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0))
                    .andExpect(jsonPath("$.pagination").exists())
                    .andExpect(jsonPath("$.pagination.page").value(1))
                    .andExpect(jsonPath("$.pagination.total").value(0));
        }

        @Test
        @DisplayName("should return paginated accounts with page parameters")
        @WithMockUser(username = "u1")
        void list_shouldReturnPaginatedAccountsWithParams() throws Exception {
            var paginatedResponse = PaginatedResponse.of(
                    List.of(new AccountSummary("acc-1", "SAVING", "THB", "123-456", "KBank", "#FF5733", 1000.00, "IN_PROGRESS")),
                    PageInfo.of(2, 10, 25)
            );
            when(accountService.listAccounts("u1", new PageRequest(2, 10))).thenReturn(paginatedResponse);

            mockMvc.perform(get("/v1/accounts")
                            .param("page", "2")
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.pagination.page").value(2))
                    .andExpect(jsonPath("$.pagination.limit").value(10))
                    .andExpect(jsonPath("$.pagination.total").value(25))
                    .andExpect(jsonPath("$.pagination.totalPages").value(3));
        }
    }

    @Nested
    @DisplayName("GET /v1/accounts/{accountId}/transactions - List Transactions")
    class ListTransactionsTests {

        @Test
        @DisplayName("should call service with default limit 20")
        @WithMockUser(username = "u1")
        void transactions_shouldCallService_withDefaultLimit() throws Exception {
            var page = new TransactionService.TransactionsPage(
                    List.of(
                            new TransactionService.TransactionItem("tx-1", "name1", "img1.png", true),
                            new TransactionService.TransactionItem("tx-2", "name2", "img2.png", false)
                    ),
                    "cursor-next"
            );
            when(transactionService.listTransactions("u1", "acc-1", null, 20)).thenReturn(page);

            mockMvc.perform(get("/v1/accounts/{accountId}/transactions", "acc-1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.items").isArray())
                    .andExpect(jsonPath("$.data.items.length()").value(2))
                    .andExpect(jsonPath("$.data.items[0].transactionId").value("tx-1"))
                    .andExpect(jsonPath("$.data.items[0].name").value("name1"))
                    .andExpect(jsonPath("$.data.nextCursor").value("cursor-next"));
        }

        @Test
        @DisplayName("should use custom limit when provided")
        @WithMockUser(username = "u1")
        void transactions_shouldUseCustomLimit() throws Exception {
            var page = new TransactionService.TransactionsPage(List.of(), null);
            when(transactionService.listTransactions("u1", "acc-1", null, 50)).thenReturn(page);

            mockMvc.perform(get("/v1/accounts/{accountId}/transactions", "acc-1")
                            .param("limit", "50"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should use cursor for pagination")
        @WithMockUser(username = "u1")
        void transactions_shouldUseCursor() throws Exception {
            var page = new TransactionService.TransactionsPage(List.of(), null);
            when(transactionService.listTransactions("u1", "acc-1", "cursor-abc", 20)).thenReturn(page);

            mockMvc.perform(get("/v1/accounts/{accountId}/transactions", "acc-1")
                            .param("cursor", "cursor-abc"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should reject limit less than 1")
        @WithMockUser(username = "u1")
        void transactions_shouldValidateLimitMin() throws Exception {
            mockMvc.perform(get("/v1/accounts/{accountId}/transactions", "acc-1")
                            .param("limit", "0"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should reject limit greater than 100")
        @WithMockUser(username = "u1")
        void transactions_shouldValidateLimitMax() throws Exception {
            mockMvc.perform(get("/v1/accounts/{accountId}/transactions", "acc-1")
                            .param("limit", "101"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /v1/accounts/goals - List Goal Accounts")
    class ListGoalsTests {

        @Test
        @DisplayName("should return paginated list of GOAL accounts mapped to GoalItem")
        @WithMockUser(username = "u1")
        void goals_shouldReturnPaginatedGoalAccounts() throws Exception {
            var paginatedResponse = PaginatedResponse.of(
                    List.of(
                            new AccountSummary("acc-goal", "GOAL", "THB", "999-111", "KBank", "#FF5733", 150.00, "IN_PROGRESS"),
                            new AccountSummary("acc-save", "SAVING", "THB", "123-456", "SCB", "#3357FF", 999.00, "IN_PROGRESS")
                    ),
                    PageInfo.of(1, 20, 2)
            );
            when(accountService.listAccounts("u1", new PageRequest(1, 20))).thenReturn(paginatedResponse);

            mockMvc.perform(get("/v1/accounts/goals"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].goalId").value("acc-goal"))
                    .andExpect(jsonPath("$.data[0].name").value("999-111"))
                    .andExpect(jsonPath("$.data[0].status").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.data[0].issuer").value("KBank"))
                    .andExpect(jsonPath("$.data[0].amount").value(150.00))
                    .andExpect(jsonPath("$.pagination").exists())
                    .andExpect(jsonPath("$.pagination.page").value(1))
                    .andExpect(jsonPath("$.pagination.total").value(2));
        }

        @Test
        @DisplayName("should return empty paginated list when no goal accounts")
        @WithMockUser(username = "u1")
        void goals_shouldReturnEmptyPaginatedListWhenNoGoals() throws Exception {
            var paginatedResponse = PaginatedResponse.of(
                    List.of(new AccountSummary("acc-save", "SAVING", "THB", "123-456", "SCB", "#3357FF", 999.00, "IN_PROGRESS")),
                    PageInfo.of(1, 20, 1)
            );
            when(accountService.listAccounts("u1", new PageRequest(1, 20))).thenReturn(paginatedResponse);

            mockMvc.perform(get("/v1/accounts/goals"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0))
                    .andExpect(jsonPath("$.pagination").exists())
                    .andExpect(jsonPath("$.pagination.total").value(1));
        }

        @Test
        @DisplayName("should handle mixed case GOAL type")
        @WithMockUser(username = "u1")
        void goals_shouldHandleMixedCaseType() throws Exception {
            var paginatedResponse = PaginatedResponse.of(
                    List.of(
                            new AccountSummary("acc-goal1", "goal", "THB", "111-222", "KBank", "#FF5733", 100.00, "IN_PROGRESS"),
                            new AccountSummary("acc-goal2", "Goal", "THB", "333-444", "SCB", "#3357FF", 200.00, "IN_PROGRESS")
                    ),
                    PageInfo.of(1, 20, 2)
            );
            when(accountService.listAccounts("u1", new PageRequest(1, 20))).thenReturn(paginatedResponse);

            mockMvc.perform(get("/v1/accounts/goals"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.pagination").exists())
                    .andExpect(jsonPath("$.pagination.total").value(2));
        }
    }

    @Nested
    @DisplayName("GET /v1/accounts/loans - List Loan Accounts")
    class ListLoansTests {

        @Test
        @DisplayName("should return only LOAN accounts mapped to LoanItem")
        @WithMockUser(username = "u1")
        void loans_shouldReturnOnlyLoanAccounts() throws Exception {
            var paginatedResponse = PaginatedResponse.of(
                    List.of(
                            new AccountSummary("acc-loan", "LOAN", "THB", "111-222", "KBank", "#FF5733", 2500.00, "ACTIVE"),
                            new AccountSummary("acc-goal", "GOAL", "THB", "999-111", "SCB", "#3357FF", 150.00, "ACTIVE")
                    ),
                    PageInfo.of(1, 20, 2)
            );
            when(accountService.listAccounts("u1", new PageRequest(1, 20))).thenReturn(paginatedResponse);

            mockMvc.perform(get("/v1/accounts/loans"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].loanId").value("acc-loan"))
                    .andExpect(jsonPath("$.data[0].name").value("111-222"))
                    .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
                    .andExpect(jsonPath("$.data[0].outstandingAmount").value(2500.00))
                    .andExpect(jsonPath("$.pagination").exists())
                    .andExpect(jsonPath("$.pagination.page").value(1))
                    .andExpect(jsonPath("$.pagination.total").value(2));
        }

        @Test
        @DisplayName("should return empty list when no loan accounts")
        @WithMockUser(username = "u1")
        void loans_shouldReturnEmptyListWhenNoLoans() throws Exception {
            var paginatedResponse = PaginatedResponse.of(
                    List.of(new AccountSummary("acc-save", "SAVING", "THB", "123-456", "SCB", "#3357FF", 999.00, "IN_PROGRESS")),
                    PageInfo.of(1, 20, 1)
            );
            when(accountService.listAccounts("u1", new PageRequest(1, 20))).thenReturn(paginatedResponse);

            mockMvc.perform(get("/v1/accounts/loans"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0))
                    .andExpect(jsonPath("$.pagination").exists())
                    .andExpect(jsonPath("$.pagination.total").value(1));
        }

        @Test
        @DisplayName("should return multiple loan accounts")
        @WithMockUser(username = "u1")
        void loans_shouldReturnMultipleLoans() throws Exception {
            var paginatedResponse = PaginatedResponse.of(
                    List.of(
                            new AccountSummary("acc-loan1", "LOAN", "THB", "111-222", "KBank", "#FF5733", 2500.00, "IN_PROGRESS"),
                            new AccountSummary("acc-loan2", "LOAN", "THB", "333-444", "SCB", "#3357FF", 5000.00, "IN_PROGRESS")
                    ),
                    PageInfo.of(1, 20, 2)
            );
            when(accountService.listAccounts("u1", new PageRequest(1, 20))).thenReturn(paginatedResponse);

            mockMvc.perform(get("/v1/accounts/loans"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].loanId").value("acc-loan1"))
                    .andExpect(jsonPath("$.data[1].loanId").value("acc-loan2"))
                    .andExpect(jsonPath("$.pagination").exists())
                    .andExpect(jsonPath("$.pagination.total").value(2));
        }
    }

    @Nested
    @DisplayName("GET /v1/accounts/payees - List Payees")
    class ListPayeesTests {

        @Test
        @DisplayName("should use default limit 10")
        @WithMockUser(username = "u1")
        void payees_shouldUseDefaultLimit10() throws Exception {
            var paginatedResponse = PaginatedResponse.of(
                    List.of(
                            new PayeeItem("p1", "name1", "img1.png", true),
                            new PayeeItem("p2", "name2", "img2.png", false)
                    ),
                    PageInfo.of(1, 10, 2)
            );
            when(accountService.listQuickPayees("u1", new PageRequest(1, 10))).thenReturn(paginatedResponse);

            mockMvc.perform(get("/v1/accounts/payees"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].payeeId").value("p1"))
                    .andExpect(jsonPath("$.data[0].name").value("name1"))
                    .andExpect(jsonPath("$.data[0].favorite").value(true))
                    .andExpect(jsonPath("$.data[1].payeeId").value("p2"))
                    .andExpect(jsonPath("$.data[1].favorite").value(false))
                    .andExpect(jsonPath("$.pagination").exists())
                    .andExpect(jsonPath("$.pagination.page").value(1))
                    .andExpect(jsonPath("$.pagination.limit").value(10));
        }

        @Test
        @DisplayName("should return paginated payees with page parameters")
        @WithMockUser(username = "u1")
        void payees_shouldReturnPaginatedPayeesWithParams() throws Exception {
            var paginatedResponse = PaginatedResponse.of(
                    List.of(new PayeeItem("p3", "name3", "img3.png", true)),
                    PageInfo.of(2, 5, 15)
            );
            when(accountService.listQuickPayees("u1", new PageRequest(2, 5))).thenReturn(paginatedResponse);

            mockMvc.perform(get("/v1/accounts/payees")
                            .param("page", "2")
                            .param("limit", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.pagination.page").value(2))
                    .andExpect(jsonPath("$.pagination.limit").value(5))
                    .andExpect(jsonPath("$.pagination.total").value(15));
        }

        @Test
        @DisplayName("should return empty paginated list when no payees")
        @WithMockUser(username = "u1")
        void payees_shouldReturnEmptyPaginatedList() throws Exception {
            var emptyResponse = PaginatedResponse.of(
                    List.<PayeeItem>of(),
                    PageInfo.of(1, 10, 0)
            );
            when(accountService.listQuickPayees("u1", new PageRequest(1, 10))).thenReturn(emptyResponse);

            mockMvc.perform(get("/v1/accounts/payees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0))
                    .andExpect(jsonPath("$.pagination").exists())
                    .andExpect(jsonPath("$.pagination.total").value(0));
        }
    }
}
