package com.micropymes.backend.followup.dto;

import com.micropymes.backend.followup.domain.FollowUpStatus;
import com.micropymes.backend.followup.domain.FollowUpType;

import java.time.Instant;
import java.util.UUID;

public record FollowUpResponse(
        UUID id,
        UUID organizationId,
        UUID opportunityId,
        UUID assignedToMemberId,
        UUID assignedToUserId,
        String assignedToName,
        FollowUpType type,
        FollowUpStatus status,
        Instant scheduledAt,
        Instant completedAt,
        String notes,
        boolean overdue,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}