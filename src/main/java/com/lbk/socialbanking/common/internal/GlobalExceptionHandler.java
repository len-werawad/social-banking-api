package com.lbk.socialbanking.common.internal;

import com.lbk.socialbanking.common.api.ApiException;
import com.lbk.socialbanking.common.api.dto.ErrorEnvelope;
import com.lbk.socialbanking.common.api.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ErrorEnvelope> buildResponse(HttpStatus status, String code, String message) {
        var body = new ErrorEnvelope(new ErrorResponse(
                status.value(),
                code,
                message,
                TraceIdProvider.getTraceId()
        ));
        return ResponseEntity.status(status).body(body);
    }

    private ResponseEntity<ErrorEnvelope> badRequest(String code, String message) {
        return buildResponse(HttpStatus.BAD_REQUEST, code, message);
    }

    private void logWarn(HttpServletRequest req, String type, String message) {
        log.warn("{} - {} {}: {}", type, req.getMethod(), req.getRequestURI(), message);
    }

    @ExceptionHandler(ApiException.class)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorEnvelope.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "error": {
                                        "status": 401,
                                        "code": "INVALID_CREDENTIALS",
                                        "message": "Invalid userId or pin",
                                        "traceId": "abc123def456"
                                      }
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Access denied",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorEnvelope.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "error": {
                                        "status": 403,
                                        "code": "ACCESS_DENIED",
                                        "message": "You do not have permission to access this resource",
                                        "traceId": "abc123def456"
                                      }
                                    }
                                    """)
                    )
            )
    })
    public ResponseEntity<ErrorEnvelope> handle(ApiException ex, HttpServletRequest req) {
        if (ex.status().is5xxServerError()) {
            log.error("API Exception [{}]: {} - {} {}", ex.code(), ex.getMessage(), req.getMethod(), req.getRequestURI(), ex);
        } else {
            log.warn("API Exception [{}]: {} - {} {}", ex.code(), ex.getMessage(), req.getMethod(), req.getRequestURI());
        }
        return buildResponse(ex.status(), ex.code(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Validation error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorEnvelope.class),
                    examples = @ExampleObject(value = """
                            {
                              "error": {
                                "status": 400,
                                "code": "VALIDATION_ERROR",
                                "message": "Invalid input parameters",
                                "traceId": "abc123def456"
                              }
                            }
                            """)
            )
    )
    public ResponseEntity<ErrorEnvelope> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        logWarn(req, "Validation error", message);
        return badRequest("VALIDATION_ERROR", message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Constraint violation",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorEnvelope.class),
                    examples = @ExampleObject(value = """
                            {
                              "error": {
                                "status": 400,
                                "code": "VALIDATION_ERROR",
                                "message": "must be greater than or equal to 1",
                                "traceId": "abc123def456"
                              }
                            }
                            """)
            )
    )
    public ResponseEntity<ErrorEnvelope> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("Validation failed");
        logWarn(req, "Constraint violation", message);
        return badRequest("VALIDATION_ERROR", message);
    }

    @ExceptionHandler({
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Missing or invalid parameter",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorEnvelope.class),
                    examples = @ExampleObject(value = """
                            {
                              "error": {
                                "status": 400,
                                "code": "VALIDATION_ERROR",
                                "message": "Required parameter 'page' is missing",
                                "traceId": "abc123def456"
                              }
                            }
                            """)
            )
    )
    public ResponseEntity<ErrorEnvelope> handleMissingOrInvalidParam(Exception ex, HttpServletRequest req) {
        String message = switch (ex) {
            case MissingRequestHeaderException e -> "Missing required header: " + e.getHeaderName();
            case MissingServletRequestParameterException e -> "Required parameter '" + e.getParameterName() + "' is missing";
            case MethodArgumentTypeMismatchException e -> {
                String type = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown";
                yield "Parameter '" + e.getName() + "' must be of type " + type;
            }
            default -> "Invalid request parameter";
        };
        logWarn(req, "Parameter error", message);
        return badRequest("VALIDATION_ERROR", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid request body",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorEnvelope.class),
                    examples = @ExampleObject(value = """
                            {
                              "error": {
                                "status": 400,
                                "code": "INVALID_REQUEST_BODY",
                                "message": "Request body is missing or malformed",
                                "traceId": "abc123def456"
                              }
                            }
                            """)
            )
    )
    public ResponseEntity<ErrorEnvelope> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        logWarn(req, "Malformed request body", ex.getMessage());
        return badRequest("INVALID_REQUEST_BODY", "Request body is missing or malformed");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ApiResponse(
            responseCode = "404",
            description = "Not Found - Resource not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorEnvelope.class),
                    examples = @ExampleObject(value = """
                            {
                              "error": {
                                "status": 404,
                                "code": "NOT_FOUND",
                                "message": "The requested resource was not found",
                                "traceId": "abc123def456"
                              }
                            }
                            """)
            )
    )
    public ResponseEntity<ErrorEnvelope> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest req) {
        log.warn("Resource not found - {} {}", req.getMethod(), req.getRequestURI());
        return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", "The requested resource was not found");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ApiResponse(
            responseCode = "405",
            description = "Method Not Allowed",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorEnvelope.class),
                    examples = @ExampleObject(value = """
                            {
                              "error": {
                                "status": 405,
                                "code": "METHOD_NOT_ALLOWED",
                                "message": "Method 'POST' is not supported. Supported: [GET]",
                                "traceId": "abc123def456"
                              }
                            }
                            """)
            )
    )
    public ResponseEntity<ErrorEnvelope> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        String message = String.format("Method '%s' is not supported. Supported: %s",
                ex.getMethod(),
                ex.getSupportedHttpMethods() != null ? ex.getSupportedHttpMethods() : "unknown");
        logWarn(req, "Method not allowed", message);
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorEnvelope.class),
                    examples = @ExampleObject(value = """
                            {
                              "error": {
                                "status": 500,
                                "code": "INTERNAL_ERROR",
                                "message": "Unexpected error",
                                "traceId": "abc123def456"
                              }
                            }
                            """)
            )
    )
    public ResponseEntity<ErrorEnvelope> handleOther(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error - {} {}", req.getMethod(), req.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected error.");
    }
}
