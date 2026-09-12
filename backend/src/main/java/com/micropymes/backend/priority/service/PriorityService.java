package com.micropymes.backend.priority.service;

import com.micropymes.backend.activity.domain.Activity;
import com.micropymes.backend.activity.repository.ActivityRepository;
import com.micropymes.backend.followup.domain.FollowUp;
import com.micropymes.backend.followup.domain.FollowUpStatus;
import com.micropymes.backend.followup.repository.FollowUpRepository;
import com.micropymes.backend.opportunity.domain.Opportunity;
import com.micropymes.backend.opportunity.domain.OpportunityStatus;
import com.micropymes.backend.opportunity.repository.OpportunityRepository;
import com.micropymes.backend.organization.service.OrganizationAccessService;
import com.micropymes.backend.priority.domain.PriorityReasonCode;
import com.micropymes.backend.priority.dto.OpportunityPriorityResponse;
import com.micropymes.backend.priority.dto.PriorityReasonResponse;
import com.micropymes.backend.quote.domain.Quote;
import com.micropymes.backend.quote.domain.QuoteStatus;
import com.micropymes.backend.quote.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PriorityService {

    private static final List<OpportunityStatus> OPEN_STATUSES =
            List.of(
                    OpportunityStatus.NEW,
                    OpportunityStatus.CONTACTED,
                    OpportunityStatus.PROPOSAL_SENT,
                    OpportunityStatus.NEGOTIATION
            );

    private final OpportunityRepository opportunityRepository;
    private final FollowUpRepository followUpRepository;
    private final ActivityRepository activityRepository;
    private final QuoteRepository quoteRepository;
    private final OrganizationAccessService accessService;
    private final PriorityRules rules;
    private final Clock clock;

    public PriorityService(
            OpportunityRepository opportunityRepository,
            FollowUpRepository followUpRepository,
            ActivityRepository activityRepository,
            QuoteRepository quoteRepository,
            OrganizationAccessService accessService,
            PriorityRules rules,
            Clock clock
    ) {
        this.opportunityRepository = opportunityRepository;
        this.followUpRepository = followUpRepository;
        this.activityRepository = activityRepository;
        this.quoteRepository = quoteRepository;
        this.accessService = accessService;
        this.rules = rules;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<OpportunityPriorityResponse> calculate(
            UUID organizationId,
            UUID userId,
            int limit
    ) {
        accessService.requireMember(
                organizationId,
                userId
        );

        int safeLimit = Math.min(
                Math.max(limit, 1),
                100
        );

        List<Opportunity> opportunities =
                opportunityRepository
                        .findByOrganization_IdAndArchivedAtIsNullAndStatusIn(
                                organizationId,
                                OPEN_STATUSES
                        );

        return opportunities.stream()
                .map(opportunity ->
                        calculateOpportunity(
                                organizationId,
                                opportunity
                        )
                )
                .sorted(
                        Comparator
                                .comparingInt(
                                        PriorityCalculation::score
                                )
                                .reversed()
                                .thenComparing(
                                        PriorityCalculation::nextFollowUpAt,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                )
                                .thenComparing(
                                        PriorityCalculation::lastActivityAt
                                )
                                .thenComparing(
                                        PriorityCalculation::createdAt
                                )
                )
                .limit(safeLimit)
                .map(PriorityCalculation::response)
                .toList();
    }

    private PriorityCalculation calculateOpportunity(
            UUID organizationId,
            Opportunity opportunity
    ) {
        Instant now = Instant.now(clock);

        List<PriorityReasonResponse> reasons =
                new ArrayList<>();

        int score = 0;

        Optional<FollowUp> nextFollowUp =
                followUpRepository
                        .findFirstByOpportunity_IdAndOrganization_IdAndStatusOrderByScheduledAtAsc(
                                opportunity.getId(),
                                organizationId,
                                FollowUpStatus.PENDING
                        );

        if (nextFollowUp.isPresent()) {

            FollowUp followUp = nextFollowUp.get();

            score += calculateFollowUpScore(
                    followUp,
                    now,
                    reasons
            );

        } else if (
                Duration.between(
                        opportunity.getCreatedAt(),
                        now
                ).toHours() >= 24
        ) {

            score += addReason(
                    reasons,
                    PriorityReasonCode.NO_PENDING_FOLLOW_UP,
                    10
            );
        }

        Optional<Activity> lastActivity =
                activityRepository
                        .findFirstByOpportunity_IdAndOrganization_IdOrderByOccurredAtDesc(
                                opportunity.getId(),
                                organizationId
                        );

        Instant lastActivityAt =
                lastActivity
                        .map(Activity::getOccurredAt)
                        .orElse(opportunity.getCreatedAt());

        long inactivityDays =
                Duration.between(
                        lastActivityAt,
                        now
                ).toDays();

        if (inactivityDays >= 14) {

            score += addReason(
                    reasons,
                    PriorityReasonCode.INACTIVE_14_PLUS_DAYS,
                    25
            );

        } else if (inactivityDays >= 7) {

            score += addReason(
                    reasons,
                    PriorityReasonCode.INACTIVE_7_TO_13_DAYS,
                    15
            );

        } else if (inactivityDays >= 3) {

            score += addReason(
                    reasons,
                    PriorityReasonCode.INACTIVE_3_TO_6_DAYS,
                    5
            );
        }

        Optional<Quote> latestSentQuote =
                quoteRepository
                        .findFirstByOpportunity_IdAndOrganization_IdAndStatusOrderBySentAtDesc(
                                opportunity.getId(),
                                organizationId,
                                QuoteStatus.SENT
                        );

        if (latestSentQuote.isPresent()) {

            long waitingDays =
                    Duration.between(
                            latestSentQuote
                                    .get()
                                    .getSentAt(),
                            now
                    ).toDays();

            if (waitingDays >= 7) {

                score += addReason(
                        reasons,
                        PriorityReasonCode.QUOTE_WAITING_7_PLUS_DAYS,
                        15
                );

            } else if (waitingDays >= 3) {

                score += addReason(
                        reasons,
                        PriorityReasonCode.QUOTE_WAITING_3_TO_6_DAYS,
                        8
                );
            }
        }

        score += calculateStatusScore(
                opportunity.getStatus(),
                reasons
        );

        score = rules.cap(score);

        OpportunityPriorityResponse response =
                new OpportunityPriorityResponse(
                        opportunity.getId(),
                        opportunity.getTitle(),
                        opportunity.getCustomer().getId(),
                        opportunity.getCustomer().getName(),
                        opportunity.getStatus(),
                        score,
                        rules.levelFor(score),
                        reasons
                );

        return new PriorityCalculation(
                response,
                score,

                nextFollowUp
                        .map(FollowUp::getScheduledAt)
                        .orElse(null),

                lastActivityAt,
                opportunity.getCreatedAt()
        );
    }

    private int calculateFollowUpScore(
            FollowUp followUp,
            Instant now,
            List<PriorityReasonResponse> reasons
    ) {
        Instant scheduledAt =
                followUp.getScheduledAt();

        LocalDate scheduledDate =
                scheduledAt
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate();

        LocalDate today =
                now.atZone(ZoneOffset.UTC)
                        .toLocalDate();

        if (scheduledAt.isBefore(now)) {

            long overdueDays =
                    ChronoUnit.DAYS.between(
                            scheduledDate,
                            today
                    );

            if (overdueDays >= 7) {
                return addReason(
                        reasons,
                        PriorityReasonCode.OVERDUE_FOLLOW_UP_LONG,
                        60
                );
            }

            if (overdueDays >= 1) {
                return addReason(
                        reasons,
                        PriorityReasonCode.OVERDUE_FOLLOW_UP,
                        45
                );
            }

            return addReason(
                    reasons,
                    PriorityReasonCode.FOLLOW_UP_DUE_TODAY,
                    35
            );
        }

        if (scheduledDate.equals(today)) {
            return addReason(
                    reasons,
                    PriorityReasonCode.FOLLOW_UP_DUE_TODAY,
                    35
            );
        }

        if (!scheduledAt.isAfter(
                now.plus(48, ChronoUnit.HOURS)
        )) {
            return addReason(
                    reasons,
                    PriorityReasonCode.FOLLOW_UP_DUE_SOON,
                    15
            );
        }

        return 0;
    }

    private int calculateStatusScore(
            OpportunityStatus status,
            List<PriorityReasonResponse> reasons
    ) {
        return switch (status) {

            case NEW ->
                    addReason(
                            reasons,
                            PriorityReasonCode.STATUS_NEW,
                            2
                    );

            case CONTACTED ->
                    addReason(
                            reasons,
                            PriorityReasonCode.STATUS_CONTACTED,
                            4
                    );

            case PROPOSAL_SENT ->
                    addReason(
                            reasons,
                            PriorityReasonCode.STATUS_PROPOSAL_SENT,
                            8
                    );

            case NEGOTIATION ->
                    addReason(
                            reasons,
                            PriorityReasonCode.STATUS_NEGOTIATION,
                            10
                    );

            default -> 0;
        };
    }

    private int addReason(
            List<PriorityReasonResponse> reasons,
            PriorityReasonCode code,
            int points
    ) {
        reasons.add(
                new PriorityReasonResponse(
                        code,
                        points
                )
        );

        return points;
    }

    private record PriorityCalculation(
            OpportunityPriorityResponse response,
            int score,
            Instant nextFollowUpAt,
            Instant lastActivityAt,
            Instant createdAt
    ) {
    }
}