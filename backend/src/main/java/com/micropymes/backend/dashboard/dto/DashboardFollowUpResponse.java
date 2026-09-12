package com.micropymes.backend.dashboard.dto;

import com.micropymes.backend.followup.domain.FollowUpType;

import java.time.Instant;
import java.util.UUID;

public record DashboardFollowUpResponse(
        UUID id,
        UUID opportunityId,
        String opportunityTitle,
        UUID customerId,
        String customerName,
        FollowUpType type,
        Instant scheduledAt,
        UUID assignedToMemberId,
        String assignedToName
) {
}