package com.lbk.wallet.account.api.dto;

public record PayeeItem(
        String payeeId,
        String name,
        String image,
        boolean favorite
) {
}
