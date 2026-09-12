package com.micropymes.backend.customer.dto;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        UUID organizationId,
        String name,
        String companyName,
        String email,
        String phone,
        String notes,
        Long version,
        Instant createdAt,
        Instant updatedAt,
        Instant archivedAt
) {
}