package com.lbk.wallet.common.internal;

import com.lbk.wallet.common.api.dto.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("SuccessResponseAdvice Tests")
class SuccessResponseAdviceTest {

    private SuccessResponseAdvice advice;
    private MethodParameter methodParameter;
    private ServerHttpResponse response;

    @BeforeEach
    void setUp() {
        advice = new SuccessResponseAdvice();
        methodParameter = mock(MethodParameter.class);
        response = mock(ServerHttpResponse.class);
    }

    @AfterEach
    void tearDown() {
        advice = null;
        methodParameter = null;
        response = null;
    }

    @Test
    @DisplayName("Should always return true for supports() method")
    void shouldAlwaysSupport() {
        boolean result = advice.supports(methodParameter, null);

        assertTrue(result);
    }

    @Test
    @DisplayName("Should wrap plain object in SuccessResponse")
    void shouldWrapPlainObjectInSuccessResponse() {
        String body = "test data";
        ServerHttpRequest request = mockRequest("/v1/test");

        Object result = advice.beforeBodyWrite(body, methodParameter, MediaType.APPLICATION_JSON, null, request, response);

        assertInstanceOf(SuccessResponse.class, result);
        SuccessResponse<?> successResponse = (SuccessResponse<?>) result;
        assertEquals("test data", successResponse.data());
    }

    @Test
    @DisplayName("Should not wrap null body")
    void shouldNotWrapNullBody() {
        ServerHttpRequest request = mockRequest("/v1/test");

        Object result = advice.beforeBodyWrite(null, methodParameter, MediaType.APPLICATION_JSON, null, request, response);

        assertNull(result);
    }

    @Test
    @DisplayName("Should not wrap existing SuccessResponse")
    void shouldNotWrapExistingSuccessResponse() {
        SuccessResponse<String> body = SuccessResponse.of("test");
        ServerHttpRequest request = mockRequest("/v1/test");

        Object result = advice.beforeBodyWrite(body, methodParameter, MediaType.APPLICATION_JSON, null, request, response);

        assertSame(body, result);
    }

    @Test
    @DisplayName("Should not wrap ErrorEnvelope")
    void shouldNotWrapErrorEnvelope() {
        ErrorResponse errorResponse = new ErrorResponse(400, "ERROR_CODE", "Error message", "trace-123");
        ErrorEnvelope body = new ErrorEnvelope(errorResponse);
        ServerHttpRequest request = mockRequest("/v1/test");

        Object result = advice.beforeBodyWrite(body, methodParameter, MediaType.APPLICATION_JSON, null, request, response);

        assertSame(body, result);
    }

    @Test
    @DisplayName("Should not wrap PaginatedResponse")
    void shouldNotWrapPaginatedResponse() {
        PageInfo pageInfo = PageInfo.of(1, 10, 2);
        PaginatedResponse<String> body = new PaginatedResponse<>(
                List.of("item1", "item2"),
                pageInfo
        );
        ServerHttpRequest request = mockRequest("/v1/test");

        Object result = advice.beforeBodyWrite(body, methodParameter, MediaType.APPLICATION_JSON, null, request, response);

        assertSame(body, result);
    }

    @Test
    @DisplayName("Should bypass Swagger UI paths")
    void shouldBypassSwaggerUiPaths() {
        String body = "swagger content";

        ServerHttpRequest swaggerRequest = mockRequest("/swagger-ui/index.html");
        Object swaggerResult = advice.beforeBodyWrite(body, methodParameter, MediaType.APPLICATION_JSON, null, swaggerRequest, response);
        assertSame(body, swaggerResult);

        ServerHttpRequest swaggerHtmlRequest = mockRequest("/swagger-ui.html");
        Object swaggerHtmlResult = advice.beforeBodyWrite(body, methodParameter, MediaType.APPLICATION_JSON, null, swaggerHtmlRequest, response);
        assertSame(body, swaggerHtmlResult);
    }

    @Test
    @DisplayName("Should bypass OpenAPI docs paths")
    void shouldBypassOpenApiDocsPaths() {
        String body = "api docs content";
        ServerHttpRequest request = mockRequest("/v3/api-docs");

        Object result = advice.beforeBodyWrite(body, methodParameter, MediaType.APPLICATION_JSON, null, request, response);

        assertSame(body, result);
    }

    private ServerHttpRequest mockRequest(String path) {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getURI()).thenReturn(URI.create(path));
        return request;
    }
}
