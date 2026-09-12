package com.micropymes.backend.dashboard.controller;

import com.micropymes.backend.auth.security.AuthenticatedUser;
import com.micropymes.backend.dashboard.dto.DashboardResponse;
import com.micropymes.backend.dashboard.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@RestController
@Tag(name = "Dashboard", description = "Dashboard management")
@RequestMapping(
        "/api/v1/organizations/{organizationId}/dashboard"
)
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/today")
    public DashboardResponse today(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return dashboardService.today(
                organizationId,
                principal.userId()
        );
    }
}