package com.lbk.wallet.common.internal;

import org.slf4j.MDC;

import java.util.Optional;

public final class TraceIdProvider {

    private TraceIdProvider() {
    }

    public static String getTraceId() {
        return Optional.ofNullable(MDC.get("traceId"))
                .orElse("N/A");
    }
}