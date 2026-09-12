package com.micropymes.backend.activity.service;

import com.micropymes.backend.activity.domain.Activity;
import com.micropymes.backend.activity.domain.ActivityType;
import com.micropymes.backend.activity.dto.ActivityResponse;
import com.micropymes.backend.activity.dto.CreateActivityRequest;
import com.micropymes.backend.activity.repository.ActivityRepository;
import com.micropymes.backend.common.error.ApiException;
import com.micropymes.backend.common.error.ErrorCode;
import com.micropymes.backend.opportunity.domain.Opportunity;
import com.micropymes.backend.opportunity.repository.OpportunityRepository;
import com.micropymes.backend.organization.domain.OrganizationMember;
import com.micropymes.backend.organization.service.OrganizationAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ActivityService {

    private static final Set<ActivityType> MANUAL_TYPES =
            EnumSet.of(
                    ActivityType.NOTE,
                    ActivityType.CALL,
                    ActivityType.EMAIL,
                    ActivityType.MEETING,
                    ActivityType.OTHER
            );

    private final ActivityRepository activityRepository;
    private final OpportunityRepository opportunityRepository;
    private final OrganizationAccessService accessService;

    public ActivityService(
            ActivityRepository activityRepository,
            OpportunityRepository opportunityRepository,
            OrganizationAccessService accessService
    ) {
        this.activityRepository = activityRepository;
        this.opportunityRepository = opportunityRepository;
        this.accessService = accessService;
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> findAll(
            UUID organizationId,
            UUID opportunityId,
            UUID userId
    ) {
        accessService.requireMember(
                organizationId,
                userId
        );

        findOpportunity(
                organizationId,
                opportunityId
        );

        return activityRepository
                .findByOpportunity_IdAndOrganization_IdOrderByOccurredAtDesc(
                        opportunityId,
                        organizationId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ActivityResponse create(
            UUID organizationId,
            UUID opportunityId,
            UUID userId,
            CreateActivityRequest request
    ) {
        OrganizationMember member =
                accessService.requireMember(
                        organizationId,
                        userId
                );

        Opportunity opportunity =
                findOpportunity(
                        organizationId,
                        opportunityId
                );

        if (opportunity.isArchived()) {
            throw new ApiException(
                    ErrorCode.OPPORTUNITY_ARCHIVED
            );
        }

        if (opportunity.isClosed()) {
            throw new ApiException(
                    ErrorCode.OPPORTUNITY_CLOSED
            );
        }

        if (!MANUAL_TYPES.contains(request.type())) {
            throw new ApiException(
                    ErrorCode.INVALID_MANUAL_ACTIVITY_TYPE
            );
        }

        Activity activity =
                new Activity(
                        member.getOrganization(),
                        opportunity,
                        member,
                        request.type(),
                        normalize(request.description())
                );

        activityRepository.save(activity);

        return toResponse(activity);
    }

    private Opportunity findOpportunity(
            UUID organizationId,
            UUID opportunityId
    ) {
        return opportunityRepository
                .findByIdAndOrganization_Id(
                        opportunityId,
                        organizationId
                )
                .orElseThrow(() ->
                        new ApiException(
                                ErrorCode.OPPORTUNITY_NOT_FOUND
                        )
                );
    }

    private ActivityResponse toResponse(
            Activity activity
    ) {
        OrganizationMember actor =
                activity.getActorMember();

        return new ActivityResponse(
                activity.getId(),
                activity.getOrganization().getId(),
                activity.getOpportunity().getId(),

                actor != null
                        ? actor.getId()
                        : null,

                actor != null
                        ? actor.getUser().getId()
                        : null,

                actor != null
                        ? actor.getUser().getFirstName()
                            + " "
                            + actor.getUser().getLastName()
                        : null,

                activity.getType(),
                activity.getDescription(),
                activity.getOccurredAt(),
                activity.getCreatedAt()
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}