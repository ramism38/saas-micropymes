package com.micropymes.backend.followup.dto;

import com.micropymes.backend.followup.domain.FollowUpType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreateFollowUpRequest(

        @NotNull
        FollowUpType type,

        @NotNull
        Instant scheduledAt,

        UUID assignedToMemberId,

        String notes

) {
}