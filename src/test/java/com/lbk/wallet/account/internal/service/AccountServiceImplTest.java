package com.lbk.wallet.account.internal.service;

import com.lbk.wallet.account.api.dto.AccountSummary;
import com.lbk.wallet.account.api.dto.PayeeItem;
import com.lbk.wallet.account.internal.persistence.entity.AccountBalanceEntity;
import com.lbk.wallet.account.internal.persistence.entity.AccountDetailEntity;
import com.lbk.wallet.account.internal.persistence.entity.AccountEntity;
import com.lbk.wallet.account.internal.persistence.entity.AccountFlagEntity;
import com.lbk.wallet.account.internal.persistence.repo.AccountBalanceRepository;
import com.lbk.wallet.account.internal.persistence.repo.AccountDetailRepository;
import com.lbk.wallet.account.internal.persistence.repo.AccountFlagRepository;
import com.lbk.wallet.account.internal.persistence.repo.AccountRepository;
import com.lbk.wallet.transaction.api.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    private static final String USER_ID = "user123";
    private static final String FAVORITE = "FAVORITE";

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountBalanceRepository balanceRepository;

    @Mock
    private AccountDetailRepository detailRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AccountFlagRepository flagRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Nested
    @DisplayName("listAccounts method tests")
    class ListAccountsTests {

        @Test
        @DisplayName("should return list of accounts with balances and details")
        void listAccounts_success() {

            AccountEntity account1 = newAccount("acc-1", USER_ID, "SAVING", "THB", "123-456", "KBank");
            AccountEntity account2 = newAccount("acc-2", USER_ID, "GOAL", "THB", "789-012", "SCB");

            AccountBalanceEntity balance1 = newBalance("acc-1", USER_ID, bd("1000.50"));
            AccountBalanceEntity balance2 = newBalance("acc-2", USER_ID, bd("500.00"));

            AccountDetailEntity detail1 = newDetail("acc-1", USER_ID, "#FF5733");
            AccountDetailEntity detail2 = newDetail("acc-2", USER_ID, "#3357FF");

            when(accountRepository.findByUserId(USER_ID)).thenReturn(List.of(account1, account2));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of(balance1, balance2));
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of(detail1, detail2));

            List<AccountSummary> result = accountService.listAccounts(USER_ID);

            assertThat(result).hasSize(2);

            AccountSummary summary1 = result.getFirst();
            assertThat(summary1.accountId()).isEqualTo("acc-1");
            assertThat(summary1.type()).isEqualTo("SAVING");
            assertThat(summary1.currency()).isEqualTo("THB");
            assertThat(summary1.accountNumber()).isEqualTo("123-456");
            assertThat(summary1.issuer()).isEqualTo("KBank");
            assertThat(summary1.color()).isEqualTo("#FF5733");
            assertThat(summary1.amount()).isEqualTo(1000.50);

            AccountSummary summary2 = result.get(1);
            assertThat(summary2.accountId()).isEqualTo("acc-2");
            assertThat(summary2.type()).isEqualTo("GOAL");
            assertThat(summary2.color()).isEqualTo("#3357FF");
            assertThat(summary2.amount()).isEqualTo(500.00);
        }

        @Test
        @DisplayName("should return empty list when user has no accounts")
        void listAccounts_emptyList() {
            String userId = "user-no-accounts";

            when(accountRepository.findByUserId(userId)).thenReturn(List.of());
            when(balanceRepository.findByUserId(userId)).thenReturn(List.of());
            when(detailRepository.findByUserId(userId)).thenReturn(List.of());

            List<AccountSummary> result = accountService.listAccounts(userId);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should handle account without balance (default to 0.0)")
        void listAccounts_withoutBalance() {
            AccountEntity account = newAccount("acc-1", USER_ID, "SAVING", "THB", "123-456", "KBank");

            when(accountRepository.findByUserId(USER_ID)).thenReturn(List.of(account));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of());
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of());

            List<AccountSummary> result = accountService.listAccounts(USER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().amount()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should handle account without detail (null color)")
        void listAccounts_withoutDetail() {
            AccountEntity account = newAccount("acc-1", USER_ID, "SAVING", "THB", "123-456", "KBank");
            AccountBalanceEntity balance = newBalance("acc-1", USER_ID, bd("1000.00"));

            when(accountRepository.findByUserId(USER_ID)).thenReturn(List.of(account));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of(balance));
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of());

            List<AccountSummary> result = accountService.listAccounts(USER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().color()).isNull();
        }

        @Test
        @DisplayName("should handle multiple accounts with partial data")
        void listAccounts_partialData() {
            AccountEntity account1 = newAccount("acc-1", USER_ID, "SAVING", "THB", "123-456", "KBank");
            AccountEntity account2 = newAccount("acc-2", USER_ID, "GOAL", "THB", "789-012", "SCB");
            AccountEntity account3 = newAccount("acc-3", USER_ID, "LOAN", "THB", "555-666", "BBL");

            AccountBalanceEntity balance1 = newBalance("acc-1", USER_ID, bd("1000.00"));
            AccountBalanceEntity balance3 = newBalance("acc-3", USER_ID, bd("5000.00"));

            AccountDetailEntity detail2 = newDetail("acc-2", USER_ID, "#3357FF");

            when(accountRepository.findByUserId(USER_ID)).thenReturn(List.of(account1, account2, account3));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of(balance1, balance3));
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of(detail2));

            List<AccountSummary> result = accountService.listAccounts(USER_ID);

            assertThat(result).hasSize(3);

            assertThat(result.getFirst().accountId()).isEqualTo("acc-1");
            assertThat(result.getFirst().amount()).isEqualTo(1000.00);
            assertThat(result.getFirst().color()).isNull();

            assertThat(result.get(1).accountId()).isEqualTo("acc-2");
            assertThat(result.get(1).amount()).isEqualTo(0.0);
            assertThat(result.get(1).color()).isEqualTo("#3357FF");

            assertThat(result.get(2).accountId()).isEqualTo("acc-3");
            assertThat(result.get(2).amount()).isEqualTo(5000.00);
            assertThat(result.get(2).color()).isNull();
        }
    }

    @Nested
    @DisplayName("listQuickPayees method tests")
    class ListQuickPayeesTests {

        @Test
        @DisplayName("should return payees with favorite flags")
        void listQuickPayees_withFavorites() {
            int limit = 5;

            TransactionService.TransactionSummary tx1 = new TransactionService.TransactionSummary("tx-1", "John Doe", "img1.png");
            TransactionService.TransactionSummary tx2 = new TransactionService.TransactionSummary("tx-2", "Jane Smith", "img2.png");
            TransactionService.TransactionSummary tx3 = new TransactionService.TransactionSummary("tx-3", "Bob Wilson", "img3.png");

            AccountFlagEntity flag1 = newFlag("tx-1", USER_ID, FAVORITE, "true");
            AccountFlagEntity flag3 = newFlag("tx-3", USER_ID, FAVORITE, "true");

            when(transactionService.listTransactionSummaries(USER_ID)).thenReturn(List.of(tx1, tx2, tx3));
            when(flagRepository.findByUserIdAndFlagType(USER_ID, FAVORITE)).thenReturn(List.of(flag1, flag3));

            List<PayeeItem> result = accountService.listQuickPayees(USER_ID, limit);

            assertThat(result).hasSize(3);

            assertThat(result.getFirst().payeeId()).isEqualTo("tx-1");
            assertThat(result.getFirst().name()).isEqualTo("John Doe");
            assertThat(result.getFirst().image()).isEqualTo("img1.png");
            assertThat(result.getFirst().favorite()).isTrue();

            assertThat(result.get(1).payeeId()).isEqualTo("tx-2");
            assertThat(result.get(1).favorite()).isFalse();

            assertThat(result.get(2).payeeId()).isEqualTo("tx-3");
            assertThat(result.get(2).favorite()).isTrue();
        }

        @Test
        @DisplayName("should limit payees to specified limit")
        void listQuickPayees_respectsLimit() {
            int limit = 2;

            TransactionService.TransactionSummary tx1 = new TransactionService.TransactionSummary("tx-1", "John Doe", "img1.png");
            TransactionService.TransactionSummary tx2 = new TransactionService.TransactionSummary("tx-2", "Jane Smith", "img2.png");
            TransactionService.TransactionSummary tx3 = new TransactionService.TransactionSummary("tx-3", "Bob Wilson", "img3.png");

            when(transactionService.listTransactionSummaries(USER_ID)).thenReturn(List.of(tx1, tx2, tx3));
            when(flagRepository.findByUserIdAndFlagType(USER_ID, FAVORITE)).thenReturn(List.of());

            List<PayeeItem> result = accountService.listQuickPayees(USER_ID, limit);

            assertThat(result).hasSize(2);
            assertThat(result.getFirst().payeeId()).isEqualTo("tx-1");
            assertThat(result.get(1).payeeId()).isEqualTo("tx-2");
        }

        @Test
        @DisplayName("should return empty list when no transactions")
        void listQuickPayees_noTransactions() {

            String userId = "user-no-tx";
            int limit = 10;

            when(transactionService.listTransactionSummaries(userId)).thenReturn(List.of());
            when(flagRepository.findByUserIdAndFlagType(userId, FAVORITE)).thenReturn(List.of());

            List<PayeeItem> result = accountService.listQuickPayees(userId, limit);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should handle no favorites (all false)")
        void listQuickPayees_noFavorites() {

            int limit = 5;

            TransactionService.TransactionSummary tx1 = new TransactionService.TransactionSummary("tx-1", "John Doe", "img1.png");
            TransactionService.TransactionSummary tx2 = new TransactionService.TransactionSummary("tx-2", "Jane Smith", "img2.png");

            when(transactionService.listTransactionSummaries(USER_ID)).thenReturn(List.of(tx1, tx2));
            when(flagRepository.findByUserIdAndFlagType(USER_ID, FAVORITE)).thenReturn(List.of());


            List<PayeeItem> result = accountService.listQuickPayees(USER_ID, limit);


            assertThat(result).hasSize(2);
            assertThat(result.getFirst().favorite()).isFalse();
            assertThat(result.get(1).favorite()).isFalse();
        }

        @Test
        @DisplayName("should handle limit 1")
        void listQuickPayees_limitOne() {

            int limit = 1;

            TransactionService.TransactionSummary tx1 = new TransactionService.TransactionSummary("tx-1", "John Doe", "img1.png");
            TransactionService.TransactionSummary tx2 = new TransactionService.TransactionSummary("tx-2", "Jane Smith", "img2.png");

            when(transactionService.listTransactionSummaries(USER_ID)).thenReturn(List.of(tx1, tx2));
            when(flagRepository.findByUserIdAndFlagType(USER_ID, FAVORITE)).thenReturn(List.of());


            List<PayeeItem> result = accountService.listQuickPayees(USER_ID, limit);


            assertThat(result).hasSize(1);
            assertThat(result.getFirst().payeeId()).isEqualTo("tx-1");
        }
    }

    @Nested
    @DisplayName("getBalancesByUserId method tests")
    class GetBalancesByUserIdTests {

        @Test
        @DisplayName("should return map of account balances")
        void getBalancesByUserId_success() {

            AccountBalanceEntity balance1 = newBalance("acc-1", USER_ID, bd("1000.50"));
            AccountBalanceEntity balance2 = newBalance("acc-2", USER_ID, bd("2500.75"));
            AccountBalanceEntity balance3 = newBalance("acc-3", USER_ID, bd("500.00"));

            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of(balance1, balance2, balance3));


            Map<String, BigDecimal> result = accountService.getBalancesByUserId(USER_ID);


            assertThat(result).hasSize(3);
            assertThat(result.get("acc-1")).isEqualByComparingTo(bd("1000.50"));
            assertThat(result.get("acc-2")).isEqualByComparingTo(bd("2500.75"));
            assertThat(result.get("acc-3")).isEqualByComparingTo(bd("500.00"));
        }

        @Test
        @DisplayName("should return empty map when user has no balances")
        void getBalancesByUserId_emptyMap() {

            String userId = "user-no-balance";

            when(balanceRepository.findByUserId(userId)).thenReturn(List.of());


            Map<String, BigDecimal> result = accountService.getBalancesByUserId(userId);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should handle single balance")
        void getBalancesByUserId_singleBalance() {

            AccountBalanceEntity balance = newBalance("acc-1", USER_ID, bd("9999.99"));

            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of(balance));

            Map<String, BigDecimal> result = accountService.getBalancesByUserId(USER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get("acc-1")).isEqualByComparingTo(bd("9999.99"));
        }

        @Test
        @DisplayName("should handle zero balance")
        void getBalancesByUserId_zeroBalance() {

            AccountBalanceEntity balance = newBalance("acc-1", USER_ID, BigDecimal.ZERO);

            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of(balance));


            Map<String, BigDecimal> result = accountService.getBalancesByUserId(USER_ID);


            assertThat(result).hasSize(1);
            assertThat(result.get("acc-1")).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should handle negative balance")
        void getBalancesByUserId_negativeBalance() {

            AccountBalanceEntity balance = newBalance("acc-1", USER_ID, bd("-500.00"));

            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of(balance));


            Map<String, BigDecimal> result = accountService.getBalancesByUserId(USER_ID);


            assertThat(result).hasSize(1);
            assertThat(result.get("acc-1")).isEqualByComparingTo(bd("-500.00"));
        }
    }

    @Nested
    @DisplayName("listGoalAccounts method tests")
    class ListGoalAccountsTests {

        @Test
        @DisplayName("should return only GOAL accounts mapped to GoalItem with same pagination")
        void listGoalAccounts_success() {
            var pageRequest = new com.lbk.wallet.common.api.dto.PageRequest(1, 20);

            AccountEntity goalAcc = newAccount("acc-goal", USER_ID, "GOAL", "THB", "999-111", "KBank");
            AccountEntity savingAcc = newAccount("acc-save", USER_ID, "SAVING", "THB", "123-456", "SCB");

            when(accountRepository.findByUserIdAndTypeIgnoreCase(USER_ID, "GOAL", pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(goalAcc)));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of());
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of());

            var result = accountService.listGoalAccounts(USER_ID, pageRequest);

            assertThat(result.data()).hasSize(1);
        }

        @Test
        @DisplayName("should return empty data when no GOAL accounts")
        void listGoalAccounts_noGoals() {
            var pageRequest = new com.lbk.wallet.common.api.dto.PageRequest(1, 20);

            when(accountRepository.findByUserIdAndTypeIgnoreCase(USER_ID, "GOAL", pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of());
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of());

            var result = accountService.listGoalAccounts(USER_ID, pageRequest);

            assertThat(result.data()).isEmpty();
        }
    }

    @Nested
    @DisplayName("listLoanAccounts method tests")
    class ListLoanAccountsTests {

        @Test
        @DisplayName("should return only LOAN accounts mapped to LoanItem with same pagination")
        void listLoanAccounts_success() {
            var pageRequest = new com.lbk.wallet.common.api.dto.PageRequest(1, 20);

            AccountEntity loanAcc = newAccount("acc-loan", USER_ID, "LOAN", "THB", "555-666", "BBL");
            AccountBalanceEntity loanBalance = newBalance("acc-loan", USER_ID, bd("5000.00"));

            when(accountRepository.findByUserIdAndTypeIgnoreCase(USER_ID, "LOAN", pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(loanAcc)));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of(loanBalance));
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of());

            var result = accountService.listLoanAccounts(USER_ID, pageRequest);

            assertThat(result.data()).hasSize(1);
        }

        @Test
        @DisplayName("should return empty data when no LOAN accounts")
        void listLoanAccounts_noLoans() {
            var pageRequest = new com.lbk.wallet.common.api.dto.PageRequest(1, 20);

            when(accountRepository.findByUserIdAndTypeIgnoreCase(USER_ID, "LOAN", pageRequest.toPageable()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));
            when(balanceRepository.findByUserId(USER_ID)).thenReturn(List.of());
            when(detailRepository.findByUserId(USER_ID)).thenReturn(List.of());

            var result = accountService.listLoanAccounts(USER_ID, pageRequest);

            assertThat(result.data()).isEmpty();
        }
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private static AccountEntity newAccount(String accountId, String userId, String type, String currency, String accountNumber, String issuer) {
        AccountEntity account = new AccountEntity();
        account.setAccountId(accountId);
        account.setUserId(userId);
        account.setType(type);
        account.setCurrency(currency);
        account.setAccountNumber(accountNumber);
        account.setIssuer(issuer);
        return account;
    }

    private static AccountBalanceEntity newBalance(String accountId, String userId, BigDecimal amount) {
        AccountBalanceEntity balance = new AccountBalanceEntity();
        balance.setAccountId(accountId);
        balance.setUserId(userId);
        balance.setAmount(amount);
        return balance;
    }

    private static AccountDetailEntity newDetail(String accountId, String userId, String color) {
        AccountDetailEntity detail = new AccountDetailEntity();
        detail.setAccountId(accountId);
        detail.setUserId(userId);
        detail.setColor(color);
        return detail;
    }

    private static AccountFlagEntity newFlag(String accountId, String userId, String flagType, String flagValue) {
        AccountFlagEntity flag = new AccountFlagEntity();
        flag.setAccountId(accountId);
        flag.setUserId(userId);
        flag.setFlagType(flagType);
        flag.setFlagValue(flagValue);
        return flag;
    }
}
