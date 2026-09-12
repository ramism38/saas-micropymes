package com.micropymes.backend.auth.dto;

import java.util.List;
import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        List<OrganizationMembershipResponse> organizations
) {
}