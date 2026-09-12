package com.micropymes.backend.common.error;

import java.time.Instant;
import java.util.List;

public record ApiError(
        String type,
        String code,
        String title,
        int status,
        String detail,
        String instance,
        Instant timestamp,
        List<FieldValidationError> fieldErrors
) {
}