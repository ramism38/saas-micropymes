package com.micropymes.backend.followup.dto;

import com.micropymes.backend.followup.domain.FollowUpType;

import java.time.Instant;
import java.util.UUID;

public record UpdateFollowUpRequest(
        FollowUpType type,
        Instant scheduledAt,
        UUID assignedToMemberId,
        Boolean unassign,
        String notes
) {
}