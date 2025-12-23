package com.lbk.wallet.transaction.internal.service;

import com.lbk.wallet.transaction.api.TransactionService;
import com.lbk.wallet.transaction.internal.persistence.entity.TransactionEntity;
import com.lbk.wallet.transaction.internal.persistence.repo.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactions;

    public TransactionServiceImpl(TransactionRepository transactions) {
        this.transactions = transactions;
    }

    @Override
    public TransactionsPage listTransactions(String userId, String accountId, String cursor, int limit) {
        log.debug("Fetching transactions for user: {}, account: {}, limit: {}", userId, accountId, limit);

        List<TransactionEntity> all = transactions.findByUserIdOrderByTransactionIdAsc(userId);
        int start = decodeCursor(cursor);
        if (start < 0) start = 0;
        if (start > all.size()) start = all.size();
        int end = Math.min(start + limit, all.size());

        var items = all.subList(start, end).stream()
                .map(t -> new TransactionItem(t.getTransactionId(), t.getName(), t.getImage(), t.getIsBank()))
                .toList();

        String next = end < all.size() ? encodeCursor(end) : null;

        log.info("Retrieved {} transactions for user: {} (total: {}, start: {}, end: {})",
                items.size(), userId, all.size(), start, end);

        return new TransactionsPage(items, next);
    }

    @Override
    public List<TransactionSummary> listTransactionSummaries(String userId) {
        log.debug("Fetching transaction summaries for user: {}", userId);

        var summaries = transactions.findByUserIdOrderByTransactionIdAsc(userId).stream()
                .map(t -> new TransactionSummary(t.getTransactionId(), t.getName(), t.getImage()))
                .toList();

        log.debug("Retrieved {} transaction summaries for user: {}", summaries.size(), userId);
        return summaries;
    }

    private String encodeCursor(int offset) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("o:" + offset).getBytes(StandardCharsets.UTF_8));
    }

    private int decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) return 0;
        try {
            String s = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            if (!s.startsWith("o:")) return 0;
            return Integer.parseInt(s.substring(2));
        } catch (Exception e) {
            return 0;
        }
    }
}
