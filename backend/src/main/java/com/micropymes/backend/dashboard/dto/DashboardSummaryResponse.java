package com.micropymes.backend.dashboard.dto;

public record DashboardSummaryResponse(
        long openOpportunities,
        long pendingFollowUps,
        long overdueFollowUps,
        long todayFollowUps
) {
}