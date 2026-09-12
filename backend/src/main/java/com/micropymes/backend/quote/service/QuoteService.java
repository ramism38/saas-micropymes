package com.micropymes.backend.quote.service;

import com.micropymes.backend.activity.domain.Activity;
import com.micropymes.backend.activity.domain.ActivityType;
import com.micropymes.backend.activity.repository.ActivityRepository;
import com.micropymes.backend.common.error.ApiException;
import com.micropymes.backend.common.error.ErrorCode;
import com.micropymes.backend.followup.domain.FollowUp;
import com.micropymes.backend.followup.domain.FollowUpStatus;
import com.micropymes.backend.followup.repository.FollowUpRepository;
import com.micropymes.backend.opportunity.domain.Opportunity;
import com.micropymes.backend.opportunity.domain.OpportunityStatus;
import com.micropymes.backend.opportunity.repository.OpportunityRepository;
import com.micropymes.backend.organization.domain.OrganizationMember;
import com.micropymes.backend.organization.service.OrganizationAccessService;
import com.micropymes.backend.quote.domain.Quote;
import com.micropymes.backend.quote.domain.QuoteStatus;
import com.micropymes.backend.quote.dto.CreateQuoteRequest;
import com.micropymes.backend.quote.dto.QuoteResponse;
import com.micropymes.backend.quote.dto.UpdateQuoteRequest;
import com.micropymes.backend.quote.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final OpportunityRepository opportunityRepository;
    private final ActivityRepository activityRepository;
    private final FollowUpRepository followUpRepository;
    private final OrganizationAccessService accessService;
    private final Clock clock;

    public QuoteService(
            QuoteRepository quoteRepository,
            OpportunityRepository opportunityRepository,
            ActivityRepository activityRepository,
            FollowUpRepository followUpRepository,
            OrganizationAccessService accessService,
            Clock clock
    ) {
        this.quoteRepository = quoteRepository;
        this.opportunityRepository = opportunityRepository;
        this.activityRepository = activityRepository;
        this.followUpRepository = followUpRepository;
        this.accessService = accessService;
        this.clock = clock;
    }

    @Transactional
    public QuoteResponse create(
            UUID organizationId,
            UUID opportunityId,
            UUID userId,
            CreateQuoteRequest request
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

        requireOpenOpportunity(opportunity);

        Quote quote = new Quote(
                member.getOrganization(),
                opportunity,
                request.amount(),
                request.currency()
                        .trim()
                        .toUpperCase(Locale.ROOT)
        );

        quote.setExpiresAt(request.expiresAt());
        quote.setNotes(normalize(request.notes()));

        quoteRepository.save(quote);

        return toResponse(quote);
    }

    @Transactional(readOnly = true)
    public List<QuoteResponse> findAll(
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

        return quoteRepository
                .findByOpportunity_IdAndOrganization_IdOrderByCreatedAtDesc(
                        opportunityId,
                        organizationId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuoteResponse findById(
            UUID organizationId,
            UUID quoteId,
            UUID userId
    ) {
        accessService.requireMember(
                organizationId,
                userId
        );

        return toResponse(
                findQuote(
                        organizationId,
                        quoteId
                )
        );
    }

    @Transactional
    public QuoteResponse update(
            UUID organizationId,
            UUID quoteId,
            UUID userId,
            UpdateQuoteRequest request
    ) {
        accessService.requireMember(
                organizationId,
                userId
        );

        Quote quote =
                findQuote(
                        organizationId,
                        quoteId
                );

        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new ApiException(
                    ErrorCode.QUOTE_NOT_EDITABLE
            );
        }

        requireOpenOpportunity(
                quote.getOpportunity()
        );

        if (request.amount() != null) {
            quote.setAmount(request.amount());
        }

        if (request.currency() != null) {
            quote.setCurrency(
                    request.currency()
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );
        }

        if (request.expiresAt() != null) {
            quote.setExpiresAt(request.expiresAt());
        }

        if (request.notes() != null) {
            quote.setNotes(
                    normalize(request.notes())
            );
        }

        return toResponse(quote);
    }

    @Transactional
    public QuoteResponse send(
            UUID organizationId,
            UUID quoteId,
            UUID userId
    ) {
        OrganizationMember member =
                accessService.requireMember(
                        organizationId,
                        userId
                );

        Quote quote =
                findQuote(
                        organizationId,
                        quoteId
                );

        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new ApiException(
                    ErrorCode.QUOTE_NOT_SENDABLE
            );
        }

        Opportunity opportunity =
                quote.getOpportunity();

        requireOpenOpportunity(opportunity);

        Instant now = Instant.now(clock);

        if (quote.getExpiresAt() != null
                && quote.getExpiresAt().isBefore(now)) {

            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "expiresAt cannot be before the send date"
            );
        }

        quote.send(now);

        activityRepository.save(
                new Activity(
                        member.getOrganization(),
                        opportunity,
                        member,
                        ActivityType.QUOTE_SENT,
                        "Quote sent"
                )
        );

        if (opportunity.getStatus()
                == OpportunityStatus.NEW
                || opportunity.getStatus()
                == OpportunityStatus.CONTACTED) {

            OpportunityStatus previousStatus =
                    opportunity.getStatus();

            opportunity.changeStatus(
                    OpportunityStatus.PROPOSAL_SENT,
                    null,
                    now
            );

            activityRepository.save(
                    new Activity(
                            member.getOrganization(),
                            opportunity,
                            member,
                            ActivityType.STATUS_CHANGE,
                            "Status changed from "
                                    + previousStatus
                                    + " to "
                                    + OpportunityStatus.PROPOSAL_SENT
                    )
            );
        }

        return toResponse(quote);
    }

    @Transactional
    public QuoteResponse accept(
            UUID organizationId,
            UUID quoteId,
            UUID userId
    ) {
        OrganizationMember member =
                accessService.requireMember(
                        organizationId,
                        userId
                );

        Quote quote =
                findQuote(
                        organizationId,
                        quoteId
                );

        if (quote.getStatus() != QuoteStatus.SENT) {
            throw new ApiException(
                    ErrorCode.QUOTE_NOT_ACCEPTABLE
            );
        }

        Opportunity opportunity =
                quote.getOpportunity();

        requireOpenOpportunity(opportunity);

        if (quoteRepository
                .existsByOpportunity_IdAndStatus(
                        opportunity.getId(),
                        QuoteStatus.ACCEPTED
                )) {

            throw new ApiException(
                    ErrorCode.QUOTE_ALREADY_ACCEPTED
            );
        }

        Instant now = Instant.now(clock);

        quote.accept();

        OpportunityStatus previousStatus =
                opportunity.getStatus();

        opportunity.changeStatus(
                OpportunityStatus.WON,
                null,
                now
        );

        cancelPendingFollowUps(
                organizationId,
                opportunity.getId()
        );

        activityRepository.save(
                new Activity(
                        member.getOrganization(),
                        opportunity,
                        member,
                        ActivityType.STATUS_CHANGE,
                        "Status changed from "
                                + previousStatus
                                + " to "
                                + OpportunityStatus.WON
                )
        );

        return toResponse(quote);
    }

    @Transactional
    public QuoteResponse reject(
            UUID organizationId,
            UUID quoteId,
            UUID userId
    ) {
        accessService.requireMember(
                organizationId,
                userId
        );

        Quote quote =
                findQuote(
                        organizationId,
                        quoteId
                );

        if (quote.getStatus() != QuoteStatus.SENT) {
            throw new ApiException(
                    ErrorCode.QUOTE_NOT_REJECTABLE
            );
        }

        requireOpenOpportunity(
                quote.getOpportunity()
        );

        quote.reject();

        return toResponse(quote);
    }

    private void cancelPendingFollowUps(
            UUID organizationId,
            UUID opportunityId
    ) {
        List<FollowUp> followUps =
                followUpRepository
                        .findByOpportunity_IdAndOrganization_IdAndStatus(
                                opportunityId,
                                organizationId,
                                FollowUpStatus.PENDING
                        );

        followUps.forEach(FollowUp::cancel);
    }

    private Quote findQuote(
            UUID organizationId,
            UUID quoteId
    ) {
        return quoteRepository
                .findByIdAndOrganization_Id(
                        quoteId,
                        organizationId
                )
                .orElseThrow(() ->
                        new ApiException(
                                ErrorCode.QUOTE_NOT_FOUND
                        )
                );
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

    private void requireOpenOpportunity(
            Opportunity opportunity
    ) {
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

    private QuoteResponse toResponse(
            Quote quote
    ) {
        return new QuoteResponse(
                quote.getId(),
                quote.getOrganization().getId(),
                quote.getOpportunity().getId(),
                quote.getAmount(),
                quote.getCurrency(),
                quote.getStatus(),
                quote.getSentAt(),
                quote.getExpiresAt(),
                quote.getNotes(),
                quote.getVersion(),
                quote.getCreatedAt(),
                quote.getUpdatedAt()
        );
    }
}