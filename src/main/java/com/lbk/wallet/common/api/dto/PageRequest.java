package com.lbk.wallet.common.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PageRequest(
        @Min(1) Integer page,
        @Min(1) @Max(100) Integer limit
) {
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_LIMIT = 20;

    public PageRequest {
        if (page == null) page = DEFAULT_PAGE;
        if (limit == null) limit = DEFAULT_LIMIT;
    }

    public org.springframework.data.domain.Pageable toPageable() {
        return org.springframework.data.domain.PageRequest.of(page - 1, limit);
    }

    public int getOffset() {
        return (page - 1) * limit;
    }
}
