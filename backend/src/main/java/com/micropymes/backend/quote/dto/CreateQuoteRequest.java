package com.micropymes.backend.quote.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateQuoteRequest(

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount,

        @NotNull
        @Pattern(
                regexp = "^[A-Za-z]{3}$",
                message = "must be a three-letter currency code"
        )
        String currency,

        Instant expiresAt,

        String notes

) {
}