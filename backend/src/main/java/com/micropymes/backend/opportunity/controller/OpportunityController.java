package com.micropymes.backend.opportunity.controller;

import com.micropymes.backend.auth.security.AuthenticatedUser;
import com.micropymes.backend.common.dto.PageResponse;
import com.micropymes.backend.opportunity.domain.OpportunityStatus;
import com.micropymes.backend.opportunity.dto.ChangeOpportunityStatusRequest;
import com.micropymes.backend.opportunity.dto.CreateOpportunityRequest;
import com.micropymes.backend.opportunity.dto.OpportunityResponse;
import com.micropymes.backend.opportunity.dto.UpdateOpportunityRequest;
import com.micropymes.backend.opportunity.service.OpportunityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/organizations/{organizationId}/opportunities"
)
@Tag(name = "Opportunities", description = "Opportunity management")
public class OpportunityController {

    private final OpportunityService opportunityService;

    public OpportunityController(
            OpportunityService opportunityService
    ) {
        this.opportunityService =
                opportunityService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpportunityResponse create(
            @PathVariable UUID organizationId,
            @Valid
            @RequestBody CreateOpportunityRequest request,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return opportunityService.create(
                organizationId,
                principal.userId(),
                request
        );
    }

    @GetMapping
    public PageResponse<OpportunityResponse> findAll(
            @PathVariable UUID organizationId,
            @RequestParam(required = false)
            OpportunityStatus status,
            @RequestParam(required = false)
            UUID customerId,
            @RequestParam(defaultValue = "false")
            boolean archived,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "20")
            int size,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return opportunityService.findAll(
                organizationId,
                principal.userId(),
                status,
                customerId,
                archived,
                page,
                size
        );
    }

    @GetMapping("/{opportunityId}")
    public OpportunityResponse findById(
            @PathVariable UUID organizationId,
            @PathVariable UUID opportunityId,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return opportunityService.findById(
                organizationId,
                opportunityId,
                principal.userId()
        );
    }

    @PatchMapping("/{opportunityId}")
    public OpportunityResponse update(
            @PathVariable UUID organizationId,
            @PathVariable UUID opportunityId,
            @Valid
            @RequestBody UpdateOpportunityRequest request,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return opportunityService.update(
                organizationId,
                opportunityId,
                principal.userId(),
                request
        );
    }

    @PatchMapping("/{opportunityId}/status")
    public OpportunityResponse changeStatus(
            @PathVariable UUID organizationId,
            @PathVariable UUID opportunityId,
            @Valid
            @RequestBody ChangeOpportunityStatusRequest request,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return opportunityService.changeStatus(
                organizationId,
                opportunityId,
                principal.userId(),
                request
        );
    }

    @PostMapping("/{opportunityId}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(
            @PathVariable UUID organizationId,
            @PathVariable UUID opportunityId,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        opportunityService.archive(
                organizationId,
                opportunityId,
                principal.userId()
        );
    }

    @PostMapping("/{opportunityId}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restore(
            @PathVariable UUID organizationId,
            @PathVariable UUID opportunityId,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        opportunityService.restore(
                organizationId,
                opportunityId,
                principal.userId()
        );
    }
}