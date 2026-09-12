package com.micropymes.backend.organization.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(

        @Size(max = 150)
        @Pattern(
                regexp = ".*\\S.*",
                message = "must not be blank"
        )
        String name,

        @Pattern(
                regexp = "^[A-Za-z]{3}$",
                message = "must be a three-letter currency code"
        )
        String defaultCurrency

) {
}