package com.lbk.wallet.banner.internal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "banners")
@Getter
@Setter
@NoArgsConstructor
public class BannerEntity {

    @Id
    @Column(name = "banner_id", length = 50, nullable = false)
    private String bannerId;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "title")
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "image")
    private String image;

    @Column(name = "dummy_col_11")
    private String dummyCol11;
}

