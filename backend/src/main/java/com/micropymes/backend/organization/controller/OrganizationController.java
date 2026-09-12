package com.micropymes.backend.organization.controller;

import com.micropymes.backend.auth.security.AuthenticatedUser;
import com.micropymes.backend.organization.dto.OrganizationResponse;
import com.micropymes.backend.organization.dto.UpdateOrganizationRequest;
import com.micropymes.backend.organization.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organizations", description = "Organization management")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(
            OrganizationService organizationService
    ) {
        this.organizationService = organizationService;
    }

    @GetMapping
    public List<OrganizationResponse> findAll(
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return organizationService
                .findOrganizationsForUser(
                        principal.userId()
                );
    }

    @GetMapping("/{organizationId}")
    public OrganizationResponse findById(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return organizationService.findById(
                organizationId,
                principal.userId()
        );
    }

    @PatchMapping("/{organizationId}")
    public OrganizationResponse update(
            @PathVariable UUID organizationId,
            @Valid
            @RequestBody UpdateOrganizationRequest request,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return organizationService.update(
                organizationId,
                principal.userId(),
                request
        );
    }
}