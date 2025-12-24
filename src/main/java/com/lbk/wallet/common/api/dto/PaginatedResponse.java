package com.lbk.wallet.common.api.dto;

import java.util.List;

public record PaginatedResponse<T>(
        List<T> data,
        PageInfo pagination
) {
    public static <T> PaginatedResponse<T> of(List<T> data, PageInfo pagination) {
        return new PaginatedResponse<>(data, pagination);
    }

    public static <T> PaginatedResponse<T> fromSpringPage(org.springframework.data.domain.Page<T> page) {
        return new PaginatedResponse<>(
                page.getContent(),
                PageInfo.fromSpringPage(page)
        );
    }
}
