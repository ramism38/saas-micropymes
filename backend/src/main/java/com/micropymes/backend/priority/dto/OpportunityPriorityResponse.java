package com.micropymes.backend.priority.dto;

import com.micropymes.backend.opportunity.domain.OpportunityStatus;
import com.micropymes.backend.priority.domain.PriorityLevel;

import java.util.List;
import java.util.UUID;

public record OpportunityPriorityResponse(
        UUID opportunityId,
        String title,
        UUID customerId,
        String customerName,
        OpportunityStatus status,
        int score,
        PriorityLevel level,
        List<PriorityReasonResponse> reasons
) {
}