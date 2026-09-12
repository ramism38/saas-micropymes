package com.micropymes.backend.common.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(
            ApiException exception,
            HttpServletRequest request
    ) {

        ErrorCode errorCode = exception.getErrorCode();

        ApiError error = new ApiError(
                "about:blank",
                errorCode.getCode(),
                errorCode.getTitle(),
                errorCode.getStatus().value(),
                exception.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                List.of()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        List<FieldValidationError> fieldErrors =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error -> new FieldValidationError(
                                error.getField(),
                                error.getDefaultMessage()
                        ))
                        .toList();

        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;

        ApiError error = new ApiError(
                "about:blank",
                errorCode.getCode(),
                errorCode.getTitle(),
                errorCode.getStatus().value(),
                "One or more fields are invalid",
                request.getRequestURI(),
                Instant.now(),
                fieldErrors
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(error);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLocking(
            OptimisticLockingFailureException exception,
            HttpServletRequest request
    ) {

        ErrorCode errorCode =
                ErrorCode.CONCURRENT_MODIFICATION;

        ApiError error = new ApiError(
                "about:blank",
                errorCode.getCode(),
                errorCode.getTitle(),
                errorCode.getStatus().value(),
                "The resource was modified by another request",
                request.getRequestURI(),
                Instant.now(),
                List.of()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {

        log.error(
                "Unexpected error processing {}",
                request.getRequestURI(),
                exception
        );

        ErrorCode errorCode =
                ErrorCode.INTERNAL_SERVER_ERROR;

        ApiError error = new ApiError(
                "about:blank",
                errorCode.getCode(),
                errorCode.getTitle(),
                errorCode.getStatus().value(),
                "An unexpected error occurred",
                request.getRequestURI(),
                Instant.now(),
                List.of()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(error);
    }
}