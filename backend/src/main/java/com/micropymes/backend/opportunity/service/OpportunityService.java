package com.micropymes.backend.opportunity.service;

import com.micropymes.backend.activity.domain.Activity;
import com.micropymes.backend.activity.domain.ActivityType;
import com.micropymes.backend.activity.repository.ActivityRepository;
import com.micropymes.backend.common.dto.PageResponse;
import com.micropymes.backend.common.error.ApiException;
import com.micropymes.backend.common.error.ErrorCode;
import com.micropymes.backend.customer.domain.Customer;
import com.micropymes.backend.customer.repository.CustomerRepository;
import com.micropymes.backend.followup.domain.FollowUp;
import com.micropymes.backend.followup.domain.FollowUpStatus;
import com.micropymes.backend.followup.repository.FollowUpRepository;
import com.micropymes.backend.opportunity.domain.Opportunity;
import com.micropymes.backend.opportunity.domain.OpportunityStatus;
import com.micropymes.backend.opportunity.dto.ChangeOpportunityStatusRequest;
import com.micropymes.backend.opportunity.dto.CreateOpportunityRequest;
import com.micropymes.backend.opportunity.dto.OpportunityResponse;
import com.micropymes.backend.opportunity.dto.UpdateOpportunityRequest;
import com.micropymes.backend.opportunity.repository.OpportunityRepository;
import com.micropymes.backend.organization.domain.OrganizationMember;
import com.micropymes.backend.organization.service.OrganizationAccessService;
import com.micropymes.backend.quote.domain.QuoteStatus;
import com.micropymes.backend.quote.repository.QuoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final CustomerRepository customerRepository;
    private final ActivityRepository activityRepository;
    private final FollowUpRepository followUpRepository;
    private final QuoteRepository quoteRepository;
    private final OrganizationAccessService accessService;
    private final Clock clock;

    public OpportunityService(
            OpportunityRepository opportunityRepository,
            CustomerRepository customerRepository,
            ActivityRepository activityRepository,
            FollowUpRepository followUpRepository,
            QuoteRepository quoteRepository,
            OrganizationAccessService accessService,
            Clock clock
    ) {
        this.opportunityRepository = opportunityRepository;
        this.customerRepository = customerRepository;
        this.activityRepository = activityRepository;
        this.followUpRepository = followUpRepository;
        this.quoteRepository = quoteRepository;
        this.accessService = accessService;
        this.clock = clock;
    }

    @Transactional
    public OpportunityResponse create(
            UUID organizationId,
            UUID userId,
            CreateOpportunityRequest request
    ) {
        OrganizationMember member =
                accessService.requireMember(
                        organizationId,
                        userId
                );

        Customer customer = customerRepository
                .findByIdAndOrganization_Id(
                        request.customerId(),
                        organizationId
                )
                .orElseThrow(() ->
                        new ApiException(
                                ErrorCode.CUSTOMER_NOT_FOUND
                        )
                );

        if (customer.isArchived()) {
            throw new ApiException(
                    ErrorCode.CUSTOMER_ARCHIVED
            );
        }

        validateValueAndCurrency(
                request.estimatedValue(),
                request.currency()
        );

        Opportunity opportunity =
                new Opportunity(
                        member.getOrganization(),
                        customer,
                        request.title().trim()
                );

        opportunity.setDescription(
                normalize(request.description())
        );

        opportunity.setEstimatedValue(
                request.estimatedValue()
        );

        opportunity.setCurrency(
                normalizeCurrency(request.currency())
        );

        opportunityRepository.save(opportunity);

        Activity activity =
                new Activity(
                        member.getOrganization(),
                        opportunity,
                        member,
                        ActivityType.OPPORTUNITY_CREATED,
                        "Opportunity created"
                );

        activityRepository.save(activity);

        return toResponse(opportunity);
    }

    @Transactional(readOnly = true)
    public OpportunityResponse findById(
            UUID organizationId,
            UUID opportunityId,
            UUID userId
    ) {
        accessService.requireMember(
                organizationId,
                userId
        );

        return toResponse(
                findOpportunity(
                        organizationId,
                        opportunityId
                )
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<OpportunityResponse> findAll(
            UUID organizationId,
            UUID userId,
            OpportunityStatus status,
            UUID customerId,
            boolean archived,
            int page,
            int size
    ) {
        accessService.requireMember(
                organizationId,
                userId
        );

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(
                Math.max(size, 1),
                100
        );

        PageRequest pageable =
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        Page<OpportunityResponse> result =
                opportunityRepository
                        .search(
                                organizationId,
                                status,
                                customerId,
                                archived,
                                pageable
                        )
                        .map(this::toResponse);

        return PageResponse.from(result);
    }

    @Transactional
    public OpportunityResponse update(
            UUID organizationId,
            UUID opportunityId,
            UUID userId,
            UpdateOpportunityRequest request
    ) {
        accessService.requireMember(
                organizationId,
                userId
        );

        Opportunity opportunity =
                findOpportunity(
                        organizationId,
                        opportunityId
                );

        requireEditable(opportunity);

        if (request.title() != null) {
            opportunity.setTitle(
                    request.title().trim()
            );
        }

        if (request.description() != null) {
            opportunity.setDescription(
                    normalize(request.description())
            );
        }

        if (request.estimatedValue() != null
                || request.currency() != null) {

            validateValueAndCurrency(
                    request.estimatedValue(),
                    request.currency()
            );

            opportunity.setEstimatedValue(
                    request.estimatedValue()
            );

            opportunity.setCurrency(
                    normalizeCurrency(
                            request.currency()
                    )
            );
        }

        return toResponse(opportunity);
    }

    @Transactional
    public OpportunityResponse changeStatus(
            UUID organizationId,
            UUID opportunityId,
            UUID userId,
            ChangeOpportunityStatusRequest request
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

        OpportunityStatus previousStatus =
                opportunity.getStatus();

        OpportunityStatus newStatus =
                request.status();

        if (previousStatus == newStatus) {
            return toResponse(opportunity);
        }

        if (opportunity.isClosed()) {

            if (newStatus == OpportunityStatus.WON
                    || newStatus == OpportunityStatus.LOST) {
                throw new ApiException(
                        ErrorCode.INVALID_OPPORTUNITY_STATUS
                );
            }

            if (previousStatus == OpportunityStatus.WON
                    && quoteRepository
                    .existsByOpportunity_IdAndStatus(
                            opportunityId,
                            QuoteStatus.ACCEPTED
                    )) {

                throw new ApiException(
                        ErrorCode.OPPORTUNITY_HAS_ACCEPTED_QUOTE
                );
            }
        }

        String lostReason =
                newStatus == OpportunityStatus.LOST
                        ? normalize(request.lostReason())
                        : null;

        opportunity.changeStatus(
                newStatus,
                lostReason,
                Instant.now(clock)
        );

        if (newStatus == OpportunityStatus.WON
                || newStatus == OpportunityStatus.LOST) {

            cancelPendingFollowUps(
                    organizationId,
                    opportunityId
            );
        }

        String description =
                "Status changed from "
                        + previousStatus
                        + " to "
                        + newStatus;

        activityRepository.save(
                new Activity(
                        member.getOrganization(),
                        opportunity,
                        member,
                        ActivityType.STATUS_CHANGE,
                        description
                )
        );

        return toResponse(opportunity);
    }

    @Transactional
    public void archive(
            UUID organizationId,
            UUID opportunityId,
            UUID userId
    ) {
        accessService.requireMember(
                organizationId,
                userId
        );

        Opportunity opportunity =
                findOpportunity(
                        organizationId,
                        opportunityId
                );

        if (!opportunity.isArchived()) {
            opportunity.archive();
        }
    }

    @Transactional
    public void restore(
            UUID organizationId,
            UUID opportunityId,
            UUID userId
    ) {
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
            opportunity.restore();
        }
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

    private void requireEditable(
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

    private void validateValueAndCurrency(
            java.math.BigDecimal value,
            String currency
    ) {
        if ((value == null) != (currency == null)) {
            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "estimatedValue and currency must be provided together"
            );
        }
    }

    private String normalizeCurrency(
            String currency
    ) {
        if (currency == null) {
            return null;
        }

        return currency
                .trim()
                .toUpperCase(Locale.ROOT);
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

    private OpportunityResponse toResponse(
            Opportunity opportunity
    ) {
        return new OpportunityResponse(
                opportunity.getId(),
                opportunity.getOrganization().getId(),
                opportunity.getCustomer().getId(),
                opportunity.getCustomer().getName(),
                opportunity.getTitle(),
                opportunity.getDescription(),
                opportunity.getStatus(),
                opportunity.getEstimatedValue(),
                opportunity.getCurrency(),
                opportunity.getLostReason(),
                opportunity.getClosedAt(),
                opportunity.getVersion(),
                opportunity.getCreatedAt(),
                opportunity.getUpdatedAt(),
                opportunity.getArchivedAt()
        );
    }
}