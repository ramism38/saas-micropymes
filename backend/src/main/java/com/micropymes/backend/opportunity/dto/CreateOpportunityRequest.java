package com.micropymes.backend.opportunity.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOpportunityRequest(

                @NotNull UUID customerId,

                @NotBlank @Size(max = 200) String title,

                String description,

                @DecimalMin(value = "0.00") BigDecimal estimatedValue,

                @Pattern(regexp = "^[A-Za-z]{3}$", message = "currency must contain exactly 3 letters") String currency

) {
}