package com.lbk.wallet.account.internal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEntity {

    @Id
    @Column(name = "account_id")
    private String accountId;

    @Column(name = "user_id")
    private String userId;

    private String type;
    private String currency;

    @Column(name = "account_number")
    private String accountNumber;

    private String issuer;

    @Column(name = "dummy_col_3")
    private String dummyCol3;
}
