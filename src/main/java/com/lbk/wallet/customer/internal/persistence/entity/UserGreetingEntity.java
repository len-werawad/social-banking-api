package com.lbk.wallet.customer.internal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_greetings")
@Getter
@Setter
@NoArgsConstructor
public class UserGreetingEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "greeting", columnDefinition = "text")
    private String greeting;

    @Column(name = "dummy_col_2")
    private String dummyCol2;
}
