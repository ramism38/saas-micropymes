package com.micropymes.backend.followup.controller;

import com.micropymes.backend.auth.security.AuthenticatedUser;
import com.micropymes.backend.common.dto.PageResponse;
import com.micropymes.backend.followup.domain.FollowUpStatus;
import com.micropymes.backend.followup.dto.CreateFollowUpRequest;
import com.micropymes.backend.followup.dto.FollowUpResponse;
import com.micropymes.backend.followup.dto.UpdateFollowUpRequest;
import com.micropymes.backend.followup.service.FollowUpService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.UUID;

@RestController
@Tag(name = "Follow-ups", description = "Follow-up management")
@RequestMapping("/api/v1/organizations/{organizationId}")
public class FollowUpController {

    private final FollowUpService followUpService;

    public FollowUpController(
            FollowUpService followUpService
    ) {
        this.followUpService = followUpService;
    }

    @PostMapping(
            "/opportunities/{opportunityId}/follow-ups"
    )
    @ResponseStatus(HttpStatus.CREATED)
    public FollowUpResponse create(
            @PathVariable UUID organizationId,
            @PathVariable UUID opportunityId,
            @Valid
            @RequestBody CreateFollowUpRequest request,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return followUpService.create(
                organizationId,
                opportunityId,
                principal.userId(),
                request
        );
    }

    @GetMapping("/follow-ups")
    public PageResponse<FollowUpResponse> findAll(
            @PathVariable UUID organizationId,
            @RequestParam(required = false)
            FollowUpStatus status,
            @RequestParam(required = false)
            UUID assignedToMemberId,
            @RequestParam(required = false)
            UUID opportunityId,
            @RequestParam(required = false)
            Instant from,
            @RequestParam(required = false)
            Instant to,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "20")
            int size,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return followUpService.findAll(
                organizationId,
                principal.userId(),
                status,
                assignedToMemberId,
                opportunityId,
                from,
                to,
                page,
                size
        );
    }

    @GetMapping("/follow-ups/{followUpId}")
    public FollowUpResponse findById(
            @PathVariable UUID organizationId,
            @PathVariable UUID followUpId,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return followUpService.findById(
                organizationId,
                followUpId,
                principal.userId()
        );
    }

    @PatchMapping("/follow-ups/{followUpId}")
    public FollowUpResponse update(
            @PathVariable UUID organizationId,
            @PathVariable UUID followUpId,
            @Valid
            @RequestBody UpdateFollowUpRequest request,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return followUpService.update(
                organizationId,
                followUpId,
                principal.userId(),
                request
        );
    }

    @PostMapping(
            "/follow-ups/{followUpId}/complete"
    )
    public FollowUpResponse complete(
            @PathVariable UUID organizationId,
            @PathVariable UUID followUpId,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return followUpService.complete(
                organizationId,
                followUpId,
                principal.userId()
        );
    }

    @PostMapping(
            "/follow-ups/{followUpId}/cancel"
    )
    public FollowUpResponse cancel(
            @PathVariable UUID organizationId,
            @PathVariable UUID followUpId,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return followUpService.cancel(
                organizationId,
                followUpId,
                principal.userId()
        );
    }
}