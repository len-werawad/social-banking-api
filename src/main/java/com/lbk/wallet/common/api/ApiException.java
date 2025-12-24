package com.lbk.wallet.common.api;

import com.lbk.wallet.common.api.dto.ErrorDetail;
import org.springframework.http.HttpStatus;

import java.util.List;

public class ApiException extends RuntimeException {

    // Including the status field in the error response body improves clarity and reliability, especially when HTTP headers are unavailable or lost. As stated in RFC 9457 name-status,
    // the status is advisory and helps consumers interpret the error even outside the HTTP context.
    private final HttpStatus status;
    private final String code;
    private final List<ErrorDetail> details;

    public ApiException(HttpStatus status, String code, String message, List<ErrorDetail> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    public ApiException(HttpStatus status, String code, String message) {
        this(status, code, message, List.of());
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public List<ErrorDetail> details() {
        return details;
    }
}
