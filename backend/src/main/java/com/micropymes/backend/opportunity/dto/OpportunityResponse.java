package com.micropymes.backend.opportunity.dto;

import com.micropymes.backend.opportunity.domain.OpportunityStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OpportunityResponse(
        UUID id,
        UUID organizationId,
        UUID customerId,
        String customerName,
        String title,
        String description,
        OpportunityStatus status,
        BigDecimal estimatedValue,
        String currency,
        String lostReason,
        Instant closedAt,
        Long version,
        Instant createdAt,
        Instant updatedAt,
        Instant archivedAt
) {
}