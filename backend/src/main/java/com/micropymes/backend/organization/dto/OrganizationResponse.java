package com.micropymes.backend.organization.dto;

import com.micropymes.backend.organization.domain.OrganizationRole;

import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name,
        String defaultCurrency,
        boolean enabled,
        OrganizationRole role
) {
}