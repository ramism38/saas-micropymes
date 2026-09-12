package com.micropymes.backend.activity.controller;

import com.micropymes.backend.activity.dto.ActivityResponse;
import com.micropymes.backend.activity.dto.CreateActivityRequest;
import com.micropymes.backend.activity.service.ActivityService;
import com.micropymes.backend.auth.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/organizations/{organizationId}"
        + "/opportunities/{opportunityId}/activities"
)
@Tag(name = "Activities", description = "Activity management")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(
            ActivityService activityService
    ) {
        this.activityService = activityService;
    }

    @GetMapping
    public List<ActivityResponse> findAll(
            @PathVariable UUID organizationId,
            @PathVariable UUID opportunityId,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return activityService.findAll(
                organizationId,
                opportunityId,
                principal.userId()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityResponse create(
            @PathVariable UUID organizationId,
            @PathVariable UUID opportunityId,
            @Valid
            @RequestBody CreateActivityRequest request,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return activityService.create(
                organizationId,
                opportunityId,
                principal.userId(),
                request
        );
    }
}