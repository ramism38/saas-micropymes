package com.micropymes.backend.opportunity.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOpportunityRequest(

        @NotNull
        UUID customerId,

        @NotBlank
        @Size(max = 200)
        String title,

        String description,

        @DecimalMin(value = "0.00")
        BigDecimal estimatedValue,

        @Size(min = 3, max = 3)
        String currency

) {
}