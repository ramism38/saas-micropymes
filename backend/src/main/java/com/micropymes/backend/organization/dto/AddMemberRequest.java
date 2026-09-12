package com.micropymes.backend.organization.dto;

import com.micropymes.backend.organization.domain.OrganizationRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddMemberRequest(

        @NotBlank
        @Email
        String email,

        @NotNull
        OrganizationRole role

) {
}