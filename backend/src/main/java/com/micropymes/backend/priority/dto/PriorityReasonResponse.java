package com.micropymes.backend.priority.dto;

import com.micropymes.backend.priority.domain.PriorityReasonCode;

public record PriorityReasonResponse(
        PriorityReasonCode code,
        int points
) {
}