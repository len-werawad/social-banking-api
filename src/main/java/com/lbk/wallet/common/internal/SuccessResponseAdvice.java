package com.lbk.wallet.common.internal;

import com.lbk.wallet.common.api.dto.ErrorEnvelope;
import com.lbk.wallet.common.api.dto.PaginatedResponse;
import com.lbk.wallet.common.api.dto.SuccessResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class SuccessResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType contentType, Class<? extends HttpMessageConverter<?>> converterType, ServerHttpRequest request, ServerHttpResponse response) {

        String path = request.getURI().getPath();
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") || path.equals("/swagger-ui.html")) {
            return body;
        }

        if (body == null || body instanceof SuccessResponse || body instanceof ErrorEnvelope) {
            return body;
        }

        if (body instanceof PaginatedResponse) {
            return body;
        }

        return SuccessResponse.of(body);
    }
}
