package com.micropymes.backend.quote.dto;

import com.micropymes.backend.quote.domain.QuoteStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record QuoteResponse(
        UUID id,
        UUID organizationId,
        UUID opportunityId,
        BigDecimal amount,
        String currency,
        QuoteStatus status,
        Instant sentAt,
        Instant expiresAt,
        String notes,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}