package com.micropymes.backend.common.error;

public record FieldValidationError(
        String field,
        String message
) {
}