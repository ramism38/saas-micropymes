package com.micropymes.backend.dashboard.service;

import com.micropymes.backend.dashboard.dto.DashboardFollowUpResponse;
import com.micropymes.backend.dashboard.dto.DashboardResponse;
import com.micropymes.backend.dashboard.dto.DashboardSummaryResponse;
import com.micropymes.backend.followup.domain.FollowUp;
import com.micropymes.backend.followup.domain.FollowUpStatus;
import com.micropymes.backend.followup.repository.FollowUpRepository;
import com.micropymes.backend.opportunity.domain.OpportunityStatus;
import com.micropymes.backend.opportunity.repository.OpportunityRepository;
import com.micropymes.backend.organization.domain.OrganizationMember;
import com.micropymes.backend.organization.service.OrganizationAccessService;
import com.micropymes.backend.priority.dto.OpportunityPriorityResponse;
import com.micropymes.backend.priority.service.PriorityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class DashboardService {

    private static final List<OpportunityStatus> OPEN_STATUSES =
            List.of(
                    OpportunityStatus.NEW,
                    OpportunityStatus.CONTACTED,
                    OpportunityStatus.PROPOSAL_SENT,
                    OpportunityStatus.NEGOTIATION
            );

    private static final int PRIORITY_LIMIT = 5;

    private final FollowUpRepository followUpRepository;
    private final OpportunityRepository opportunityRepository;
    private final OrganizationAccessService accessService;
    private final PriorityService priorityService;
    private final Clock clock;

    public DashboardService(
            FollowUpRepository followUpRepository,
            OpportunityRepository opportunityRepository,
            OrganizationAccessService accessService,
            PriorityService priorityService,
            Clock clock
    ) {
        this.followUpRepository = followUpRepository;
        this.opportunityRepository = opportunityRepository;
        this.accessService = accessService;
        this.priorityService = priorityService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardResponse today(
            UUID organizationId,
            UUID userId
    ) {

        accessService.requireMember(
                organizationId,
                userId
        );

        Instant now = Instant.now(clock);

        LocalDate today =
                now.atZone(ZoneOffset.UTC)
                        .toLocalDate();

        Instant tomorrowStart =
                today.plusDays(1)
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant();

        long openOpportunities =
                opportunityRepository
                        .countByOrganization_IdAndArchivedAtIsNullAndStatusIn(
                                organizationId,
                                OPEN_STATUSES
                        );

        long pendingFollowUps =
                followUpRepository
                        .countByOrganization_IdAndStatus(
                                organizationId,
                                FollowUpStatus.PENDING
                        );

        long overdueFollowUps =
                followUpRepository
                        .countByOrganization_IdAndStatusAndScheduledAtBefore(
                                organizationId,
                                FollowUpStatus.PENDING,
                                now
                        );

        long todayFollowUps =
                followUpRepository
                        .countByOrganization_IdAndStatusAndScheduledAtGreaterThanEqualAndScheduledAtLessThan(
                                organizationId,
                                FollowUpStatus.PENDING,
                                now,
                                tomorrowStart
                        );

        List<DashboardFollowUpResponse> overdue =
                followUpRepository
                        .findByOrganization_IdAndStatusAndScheduledAtBeforeOrderByScheduledAtAsc(
                                organizationId,
                                FollowUpStatus.PENDING,
                                now
                        )
                        .stream()
                        .map(this::toFollowUpResponse)
                        .toList();

        List<DashboardFollowUpResponse> todayList =
                followUpRepository
                        .findByOrganization_IdAndStatusAndScheduledAtGreaterThanEqualAndScheduledAtLessThanOrderByScheduledAtAsc(
                                organizationId,
                                FollowUpStatus.PENDING,
                                now,
                                tomorrowStart
                        )
                        .stream()
                        .map(this::toFollowUpResponse)
                        .toList();

        List<OpportunityPriorityResponse> priorities =
                priorityService.calculate(
                        organizationId,
                        userId,
                        PRIORITY_LIMIT
                );

        DashboardSummaryResponse summary =
                new DashboardSummaryResponse(
                        openOpportunities,
                        pendingFollowUps,
                        overdueFollowUps,
                        todayFollowUps
                );

        return new DashboardResponse(
                now,
                summary,
                overdue,
                todayList,
                priorities
        );
    }

    private DashboardFollowUpResponse toFollowUpResponse(
            FollowUp followUp
    ) {

        OrganizationMember assigned =
                followUp.getAssignedToMember();

        return new DashboardFollowUpResponse(
                followUp.getId(),
                followUp.getOpportunity().getId(),
                followUp.getOpportunity().getTitle(),
                followUp.getOpportunity()
                        .getCustomer()
                        .getId(),
                followUp.getOpportunity()
                        .getCustomer()
                        .getName(),
                followUp.getType(),
                followUp.getScheduledAt(),

                assigned != null
                        ? assigned.getId()
                        : null,

                assigned != null
                        ? assigned.getUser().getFirstName()
                            + " "
                            + assigned.getUser().getLastName()
                        : null
        );
    }
}