package com.lbk.wallet.transaction.api;

import java.util.List;

public interface TransactionService {

    TransactionsPage listTransactions(String userId, String accountId, String cursor, int limit);

    List<TransactionSummary> listTransactionSummaries(String userId);

    record TransactionsPage(List<TransactionItem> items, String nextCursor) {
    }

    record TransactionItem(String transactionId, String name, String image, Boolean isBank) {
    }

    record TransactionSummary(String transactionId, String name, String image) {
    }
}
