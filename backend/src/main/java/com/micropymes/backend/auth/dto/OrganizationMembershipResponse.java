package com.micropymes.backend.auth.dto;

import com.micropymes.backend.organization.domain.OrganizationRole;

import java.util.UUID;

public record OrganizationMembershipResponse(
        UUID organizationId,
        String organizationName,
        OrganizationRole role
)
{
}

