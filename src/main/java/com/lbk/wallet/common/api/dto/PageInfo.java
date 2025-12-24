package com.lbk.wallet.common.api.dto;

/**
 * Pagination information for responses
 */
public record PageInfo(
        Integer page,
        Integer limit,
        Long total,
        Integer totalPages
) {
    public static PageInfo of(int page, int limit, long total) {
        int totalPages = (int) Math.ceil((double) total / limit);
        return new PageInfo(page, limit, total, totalPages);
    }

    public static PageInfo fromSpringPage(org.springframework.data.domain.Page<?> springPage) {
        return new PageInfo(
                springPage.getNumber() + 1,
                springPage.getSize(),
                springPage.getTotalElements(),
                springPage.getTotalPages()
        );
    }
}
