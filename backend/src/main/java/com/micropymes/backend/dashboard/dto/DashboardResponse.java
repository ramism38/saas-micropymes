package com.micropymes.backend.dashboard.dto;

import com.micropymes.backend.priority.dto.OpportunityPriorityResponse;

import java.time.Instant;
import java.util.List;

public record DashboardResponse(
        Instant generatedAt,
        DashboardSummaryResponse summary,
        List<DashboardFollowUpResponse> overdueFollowUps,
        List<DashboardFollowUpResponse> todayFollowUps,
        List<OpportunityPriorityResponse> priorityOpportunities
) {
}