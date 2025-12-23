package com.lbk.wallet.account.internal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "account_balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountBalanceEntity {

    @Id
    @Column(name = "account_id")
    private String accountId;

    @Column(name = "user_id")
    private String userId;

    private BigDecimal amount;

    @Column(name = "dummy_col_4")
    private String dummyCol4;
}
