package com.lbk.socialbanking.common.internal;

import com.lbk.socialbanking.common.api.ApiException;
import com.lbk.socialbanking.common.api.dto.ErrorEnvelope;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mockRequest("POST", "/v1/test");
    }

    @AfterEach
    void tearDown() {
        handler = null;
        request = null;
    }

    @Test
    @DisplayName("Should handle ApiException with 4xx status")
    void shouldHandleApiExceptionWith4xxStatus() {
        ApiException ex = new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "Invalid input data");

        ResponseEntity<ErrorEnvelope> response = handler.handle(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().error().status());
        assertEquals("INVALID_INPUT", response.getBody().error().code());
        assertEquals("Invalid input data", response.getBody().error().message());
        assertNotNull(response.getBody().error().traceId());
    }

    @Test
    @DisplayName("Should handle ApiException with 5xx status")
    void shouldHandleApiExceptionWith5xxStatus() {
        ApiException ex = new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "Internal server error");

        ResponseEntity<ErrorEnvelope> response = handler.handle(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().error().status());
        assertEquals("SERVER_ERROR", response.getBody().error().code());
        assertEquals("Internal server error", response.getBody().error().message());
        assertNotNull(response.getBody().error().traceId());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException")
    void shouldHandleMethodArgumentNotValidException() {
        FieldError fieldError = new FieldError("user", "email", "Email is required");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorEnvelope> response = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().error().status());
        assertEquals("VALIDATION_ERROR", response.getBody().error().code());
        assertEquals("Email is required", response.getBody().error().message());
        assertNotNull(response.getBody().error().traceId());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with no errors")
    void shouldHandleValidationExceptionWithNoErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorEnvelope> response = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().error().message());
        assertNotNull(response.getBody().error().traceId());
    }

    @Test
    @DisplayName("Should handle MissingRequestHeaderException")
    void shouldHandleMissingRequestHeaderException() {
        MethodParameter methodParameter = mock(MethodParameter.class);
        when(methodParameter.getNestedParameterType()).thenReturn((Class) String.class);

        MissingRequestHeaderException ex = new MissingRequestHeaderException("Authorization", methodParameter);

        ResponseEntity<ErrorEnvelope> response = handler.handleMissingOrInvalidParam(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().error().status());
        assertEquals("VALIDATION_ERROR", response.getBody().error().code());
        assertEquals("Missing required header: Authorization", response.getBody().error().message());
        assertNotNull(response.getBody().error().traceId());
    }

    @Test
    @DisplayName("Should handle MissingServletRequestParameterException")
    void shouldHandleMissingServletRequestParameterException() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("page", "Integer");

        ResponseEntity<ErrorEnvelope> response = handler.handleMissingOrInvalidParam(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().error().status());
        assertEquals("VALIDATION_ERROR", response.getBody().error().code());
        assertEquals("Required parameter 'page' is missing", response.getBody().error().message());
        assertNotNull(response.getBody().error().traceId());
    }

    @Test
    @DisplayName("Should handle MethodArgumentTypeMismatchException")
    void shouldHandleMethodArgumentTypeMismatchException() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("limit");
        when(ex.getRequiredType()).thenReturn((Class) Integer.class);

        ResponseEntity<ErrorEnvelope> response = handler.handleMissingOrInvalidParam(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().error().status());
        assertEquals("VALIDATION_ERROR", response.getBody().error().code());
        assertEquals("Parameter 'limit' must be of type Integer", response.getBody().error().message());
        assertNotNull(response.getBody().error().traceId());
    }

    @Test
    @DisplayName("Should handle ConstraintViolationException")
    void shouldHandleConstraintViolationException() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Pin must be 6 digits");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorEnvelope> response = handler.handleConstraintViolation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().error().status());
        assertEquals("VALIDATION_ERROR", response.getBody().error().code());
        assertEquals("Pin must be 6 digits", response.getBody().error().message());
        assertNotNull(response.getBody().error().traceId());
    }

    @Test
    @DisplayName("Should handle ConstraintViolationException with no violations")
    void shouldHandleConstraintViolationWithNoViolations() {
        ConstraintViolationException ex = new ConstraintViolationException(Set.of());

        ResponseEntity<ErrorEnvelope> response = handler.handleConstraintViolation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().error().message());
    }

    @Test
    @DisplayName("Should handle HttpMessageNotReadableException")
    void shouldHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("JSON parse error");

        ResponseEntity<ErrorEnvelope> response = handler.handleMessageNotReadable(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().error().status());
        assertEquals("INVALID_REQUEST_BODY", response.getBody().error().code());
        assertEquals("Request body is missing or malformed", response.getBody().error().message());
        assertNotNull(response.getBody().error().traceId());
    }

    @Test
    @DisplayName("Should handle NoResourceFoundException")
    void shouldHandleNoResourceFoundException() throws NoResourceFoundException {
        NoResourceFoundException ex = mock(NoResourceFoundException.class);

        ResponseEntity<ErrorEnvelope> response = handler.handleNoResourceFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().error().status());
        assertEquals("NOT_FOUND", response.getBody().error().code());
        assertEquals("The requested resource was not found", response.getBody().error().message());
        assertNotNull(response.getBody().error().traceId());
    }

    @Test
    @DisplayName("Should handle HttpRequestMethodNotSupportedException")
    void shouldHandleHttpRequestMethodNotSupportedException() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST", List.of("GET", "PUT"));

        ResponseEntity<ErrorEnvelope> response = handler.handleMethodNotSupported(ex, request);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(405, response.getBody().error().status());
        assertEquals("METHOD_NOT_ALLOWED", response.getBody().error().code());
        assertTrue(response.getBody().error().message().contains("POST"));
        assertNotNull(response.getBody().error().traceId());
    }

    @Test
    @DisplayName("Should handle unexpected Exception")
    void shouldHandleUnexpectedException() {
        Exception ex = new RuntimeException("Unexpected error occurred");

        ResponseEntity<ErrorEnvelope> response = handler.handleOther(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().error().status());
        assertEquals("INTERNAL_ERROR", response.getBody().error().code());
        assertEquals("Unexpected error.", response.getBody().error().message());
        assertNotNull(response.getBody().error().traceId());
    }

    @Test
    @DisplayName("Should handle NullPointerException as unexpected error")
    void shouldHandleNullPointerException() {
        NullPointerException ex = new NullPointerException("Null value");

        ResponseEntity<ErrorEnvelope> response = handler.handleOther(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", response.getBody().error().code());
        assertNotNull(response.getBody().error().traceId());
    }

    private HttpServletRequest mockRequest(String method, String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURI()).thenReturn(uri);
        return request;
    }
}
