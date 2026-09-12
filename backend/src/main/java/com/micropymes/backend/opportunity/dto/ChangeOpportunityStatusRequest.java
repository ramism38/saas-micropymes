package com.micropymes.backend.opportunity.dto;

import com.micropymes.backend.opportunity.domain.OpportunityStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangeOpportunityStatusRequest(

        @NotNull
        OpportunityStatus status,

        @Size(max = 255)
        String lostReason

) {
}