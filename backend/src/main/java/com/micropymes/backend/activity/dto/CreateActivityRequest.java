package com.micropymes.backend.activity.dto;

import com.micropymes.backend.activity.domain.ActivityType;
import jakarta.validation.constraints.NotNull;

public record CreateActivityRequest(

        @NotNull
        ActivityType type,

        String description

) {
}