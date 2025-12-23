package com.lbk.wallet.account.api.dto;

public record GoalItem(
        String goalId,
        String name,
        String status,
        String issuer,
        double amount
) {
}
