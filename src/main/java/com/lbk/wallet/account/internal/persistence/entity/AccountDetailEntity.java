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
@Table(name = "account_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDetailEntity {

    @Id
    @Column(name = "account_id")
    private String accountId;

    @Column(name = "user_id")
    private String userId;

    private String color;

    @Column(name = "is_main_account")
    private Boolean isMainAccount;

    private Integer progress;

    @Column(name = "dummy_col_5")
    private String dummyCol5;
}
