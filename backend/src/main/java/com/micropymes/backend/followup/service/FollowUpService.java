package com.micropymes.backend.followup.service;

import com.micropymes.backend.activity.domain.Activity;
import com.micropymes.backend.activity.domain.ActivityType;
import com.micropymes.backend.activity.repository.ActivityRepository;
import com.micropymes.backend.common.dto.PageResponse;
import com.micropymes.backend.common.error.ApiException;
import com.micropymes.backend.common.error.ErrorCode;
import com.micropymes.backend.followup.domain.FollowUp;
import com.micropymes.backend.followup.domain.FollowUpStatus;
import com.micropymes.backend.followup.dto.CreateFollowUpRequest;
import com.micropymes.backend.followup.dto.FollowUpResponse;
import com.micropymes.backend.followup.dto.UpdateFollowUpRequest;
import com.micropymes.backend.followup.repository.FollowUpRepository;
import com.micropymes.backend.opportunity.domain.Opportunity;
import com.micropymes.backend.opportunity.repository.OpportunityRepository;
import com.micropymes.backend.organization.domain.OrganizationMember;
import com.micropymes.backend.organization.repository.OrganizationMemberRepository;
import com.micropymes.backend.organization.service.OrganizationAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class FollowUpService {

    private final FollowUpRepository followUpRepository;
    private final OpportunityRepository opportunityRepository;
    private final OrganizationMemberRepository memberRepository;
    private final ActivityRepository activityRepository;
    private final OrganizationAccessService accessService;
    private final Clock clock;

    public FollowUpService(
            FollowUpRepository followUpRepository,
            OpportunityRepository opportunityRepository,
            OrganizationMemberRepository memberRepository,
            ActivityRepository activityRepository,
            OrganizationAccessService accessService,
            Clock clock
    ) {
        this.followUpRepository = followUpRepository;
        this.opportunityRepository = opportunityRepository;
        this.memberRepository = memberRepository;
        this.activityRepository = activityRepository;
        this.accessService = accessService;
        this.clock = clock;
    }

    @Transactional
    public FollowUpResponse create(
            UUID organizationId,
            UUID opportunityId,
            UUID userId,
            CreateFollowUpRequest request
    ) {
        OrganizationMember currentMember =
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

        FollowUp followUp =
                new FollowUp(
                        currentMember.getOrganization(),
                        opportunity,
                        request.type(),
                        request.scheduledAt()
                );

        if (request.assignedToMemberId() != null) {
            followUp.setAssignedToMember(
                    findActiveMember(
                            organizationId,
                            request.assignedToMemberId()
                    )
            );
        }

        followUp.setNotes(
                normalize(request.notes())
        );

        followUpRepository.save(followUp);

        return toResponse(followUp);
    }

    @Transactional(readOnly = true)
    public FollowUpResponse findById(
            UUID organizationId,
            UUID followUpId,
            UUID userId
    ) {
        accessService.requireMember(
                organizationId,
                userId
        );

        return toResponse(
                findFollowUp(
                        organizationId,
                        followUpId
                )
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<FollowUpResponse> findAll(
            UUID organizationId,
            UUID userId,
            FollowUpStatus status,
            UUID assignedToMemberId,
            UUID opportunityId,
            Instant from,
            Instant to,
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

        Specification<FollowUp> specification =
                (root, query, cb) ->
                        cb.equal(
                                root.get("organization").get("id"),
                                organizationId
                        );

        if (status != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("status"),
                                    status
                            )
            );
        }

        if (assignedToMemberId != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("assignedToMember").get("id"),
                                    assignedToMemberId
                            )
            );
        }

        if (opportunityId != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("opportunity").get("id"),
                                    opportunityId
                            )
            );
        }

        if (from != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.greaterThanOrEqualTo(
                                    root.get("scheduledAt"),
                                    from
                            )
            );
        }

        if (to != null) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.lessThanOrEqualTo(
                                    root.get("scheduledAt"),
                                    to
                            )
            );
        }

        PageRequest pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(
                        Sort.Direction.ASC,
                        "scheduledAt"
                )
        );

        Page<FollowUpResponse> result =
                followUpRepository
                        .findAll(
                                specification,
                                pageable
                        )
                        .map(this::toResponse);

        return PageResponse.from(result);
    }

    @Transactional
    public FollowUpResponse update(
            UUID organizationId,
            UUID followUpId,
            UUID userId,
            UpdateFollowUpRequest request
    ) {
        accessService.requireMember(
                organizationId,
                userId
        );

        FollowUp followUp =
                findFollowUp(
                        organizationId,
                        followUpId
                );

        if (followUp.getStatus()
                != FollowUpStatus.PENDING) {

            throw new ApiException(
                    ErrorCode.FOLLOW_UP_NOT_EDITABLE
            );
        }

        requireOpenOpportunity(
                followUp.getOpportunity()
        );

        if (request.type() != null) {
            followUp.setType(request.type());
        }

        if (request.scheduledAt() != null) {
            followUp.setScheduledAt(
                    request.scheduledAt()
            );
        }

        if (Boolean.TRUE.equals(request.unassign())) {

            if (request.assignedToMemberId() != null) {
                throw new ApiException(
                        ErrorCode.VALIDATION_FAILED,
                        "assignedToMemberId and unassign=true cannot be used together"
                );
            }

            followUp.setAssignedToMember(null);

        } else if (request.assignedToMemberId() != null) {

            followUp.setAssignedToMember(
                    findActiveMember(
                            organizationId,
                            request.assignedToMemberId()
                    )
            );
        }

        if (request.notes() != null) {
            followUp.setNotes(
                    normalize(request.notes())
            );
        }

        return toResponse(followUp);
    }

    @Transactional
    public FollowUpResponse complete(
            UUID organizationId,
            UUID followUpId,
            UUID userId
    ) {
        OrganizationMember currentMember =
                accessService.requireMember(
                        organizationId,
                        userId
                );

        FollowUp followUp =
                findFollowUp(
                        organizationId,
                        followUpId
                );

        if (followUp.getStatus()
                != FollowUpStatus.PENDING) {

            throw new ApiException(
                    ErrorCode.FOLLOW_UP_NOT_COMPLETABLE
            );
        }

        requireOpenOpportunity(
                followUp.getOpportunity()
        );

        followUp.complete(
                Instant.now(clock)
        );

        activityRepository.save(
                new Activity(
                        currentMember.getOrganization(),
                        followUp.getOpportunity(),
                        currentMember,
                        ActivityType.FOLLOW_UP_COMPLETED,
                        "Follow-up completed"
                )
        );

        return toResponse(followUp);
    }

    @Transactional
    public FollowUpResponse cancel(
            UUID organizationId,
            UUID followUpId,
            UUID userId
    ) {
        accessService.requireMember(
                organizationId,
                userId
        );

        FollowUp followUp =
                findFollowUp(
                        organizationId,
                        followUpId
                );

        if (followUp.getStatus()
                != FollowUpStatus.PENDING) {

            throw new ApiException(
                    ErrorCode.FOLLOW_UP_NOT_CANCELLABLE
            );
        }

        followUp.cancel();

        return toResponse(followUp);
    }

    private OrganizationMember findActiveMember(
            UUID organizationId,
            UUID memberId
    ) {
        OrganizationMember member =
                memberRepository
                        .findByIdAndOrganization_Id(
                                memberId,
                                organizationId
                        )
                        .orElseThrow(() ->
                                new ApiException(
                                        ErrorCode.ASSIGNED_MEMBER_NOT_FOUND
                                )
                        );

        if (!member.isActive()) {
            throw new ApiException(
                    ErrorCode.ASSIGNED_MEMBER_INACTIVE
            );
        }

        return member;
    }

    private FollowUp findFollowUp(
            UUID organizationId,
            UUID followUpId
    ) {
        return followUpRepository
                .findByIdAndOrganization_Id(
                        followUpId,
                        organizationId
                )
                .orElseThrow(() ->
                        new ApiException(
                                ErrorCode.FOLLOW_UP_NOT_FOUND
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

    private FollowUpResponse toResponse(
            FollowUp followUp
    ) {
        OrganizationMember assigned =
                followUp.getAssignedToMember();

        boolean overdue =
                followUp.getStatus()
                        == FollowUpStatus.PENDING
                && followUp.getScheduledAt()
                        .isBefore(Instant.now(clock));

        return new FollowUpResponse(
                followUp.getId(),
                followUp.getOrganization().getId(),
                followUp.getOpportunity().getId(),

                assigned != null
                        ? assigned.getId()
                        : null,

                assigned != null
                        ? assigned.getUser().getId()
                        : null,

                assigned != null
                        ? assigned.getUser().getFirstName()
                            + " "
                            + assigned.getUser().getLastName()
                        : null,

                followUp.getType(),
                followUp.getStatus(),
                followUp.getScheduledAt(),
                followUp.getCompletedAt(),
                followUp.getNotes(),
                overdue,
                followUp.getVersion(),
                followUp.getCreatedAt(),
                followUp.getUpdatedAt()
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