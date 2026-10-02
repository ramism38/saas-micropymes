package com.micropymes.backend.opportunity.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateOpportunityRequest(

                @Size(max = 200) @Pattern(regexp = ".*\\S.*", message = "must not be blank") String title,

                String description,

                @DecimalMin(value = "0.00") BigDecimal estimatedValue,

                @Pattern(regexp = "^[A-Za-z]{3}$", message = "currency must contain exactly 3 letters") String currency

) {
}