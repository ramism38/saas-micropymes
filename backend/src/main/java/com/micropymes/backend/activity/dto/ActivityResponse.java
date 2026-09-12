package com.micropymes.backend.activity.dto;

import com.micropymes.backend.activity.domain.ActivityType;

import java.time.Instant;
import java.util.UUID;

public record ActivityResponse(
        UUID id,
        UUID organizationId,
        UUID opportunityId,
        UUID actorMemberId,
        UUID actorUserId,
        String actorName,
        ActivityType type,
        String description,
        Instant occurredAt,
        Instant createdAt
) {
}