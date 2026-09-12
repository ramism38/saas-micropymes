package com.micropymes.backend.organization.dto;

import com.micropymes.backend.organization.domain.OrganizationRole;

import java.time.Instant;
import java.util.UUID;

public record MemberResponse(
        UUID id,
        UUID userId,
        String email,
        String firstName,
        String lastName,
        OrganizationRole role,
        boolean active,
        Instant joinedAt,
        Instant leftAt
) {
}