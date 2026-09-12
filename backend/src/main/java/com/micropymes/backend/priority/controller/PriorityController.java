package com.micropymes.backend.priority.controller;

import com.micropymes.backend.auth.security.AuthenticatedUser;
import com.micropymes.backend.priority.dto.OpportunityPriorityResponse;
import com.micropymes.backend.priority.service.PriorityService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/organizations/{organizationId}"
)
public class PriorityController {

    private final PriorityService priorityService;

    public PriorityController(
            PriorityService priorityService
    ) {
        this.priorityService = priorityService;
    }

    @GetMapping("/opportunity-priorities")
    public List<OpportunityPriorityResponse> priorities(
            @PathVariable UUID organizationId,
            @RequestParam(defaultValue = "20")
            int limit,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return priorityService.calculate(
                organizationId,
                principal.userId(),
                limit
        );
    }
}