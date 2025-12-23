package com.lbk.wallet.account.api.dto;

public record AccountSummary(
        String accountId,
        String type,
        String currency,
        String accountNumber,
        String issuer,
        String color,
        double amount
) {
}
