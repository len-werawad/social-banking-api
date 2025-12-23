package com.lbk.wallet.account.api.dto;

public record LoanItem(
        String loanId,
        String name,
        String status,
        double outstandingAmount
) {
}
