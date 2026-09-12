package com.micropymes.backend.organization.dto;

import com.micropymes.backend.organization.domain.OrganizationRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(

        @NotNull
        OrganizationRole role

) {
}