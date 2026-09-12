package com.micropymes.backend.followup.service;

import com.micropymes.backend.activity.repository.ActivityRepository;
import com.micropymes.backend.common.error.ApiException;
import com.micropymes.backend.common.error.ErrorCode;
import com.micropymes.backend.customer.domain.Customer;
import com.micropymes.backend.followup.domain.FollowUp;
import com.micropymes.backend.followup.domain.FollowUpStatus;
import com.micropymes.backend.followup.domain.FollowUpType;
import com.micropymes.backend.followup.dto.CreateFollowUpRequest;
import com.micropymes.backend.followup.dto.UpdateFollowUpRequest;
import com.micropymes.backend.followup.repository.FollowUpRepository;
import com.micropymes.backend.opportunity.domain.Opportunity;
import com.micropymes.backend.opportunity.domain.OpportunityStatus;
import com.micropymes.backend.opportunity.repository.OpportunityRepository;
import com.micropymes.backend.organization.domain.Organization;
import com.micropymes.backend.organization.domain.OrganizationMember;
import com.micropymes.backend.organization.domain.OrganizationRole;
import com.micropymes.backend.organization.repository.OrganizationMemberRepository;
import com.micropymes.backend.organization.service.OrganizationAccessService;
import com.micropymes.backend.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FollowUpServiceTest {

    @Mock
    private FollowUpRepository followUpRepository;

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private OrganizationMemberRepository memberRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private OrganizationAccessService accessService;

    private FollowUpService followUpService;

    private Organization organization;
    private OrganizationMember currentMember;
    private Opportunity opportunity;

    private final UUID organizationId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID opportunityId = UUID.randomUUID();
    private final UUID followUpId = UUID.randomUUID();
    private final UUID assignedMemberId = UUID.randomUUID();

    private final Instant fixedInstant =
            Instant.parse("2026-09-12T10:00:00Z");

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        Clock clock = Clock.fixed(
                fixedInstant,
                ZoneOffset.UTC
        );

        followUpService = new FollowUpService(
                followUpRepository,
                opportunityRepository,
                memberRepository,
                activityRepository,
                accessService,
                clock
        );

        organization =
                new Organization(
                        "Empresa",
                        "EUR"
                );

        User user =
                new User(
                        "user@test.com",
                        "hash",
                        "Test",
                        "User"
                );

        currentMember =
                new OrganizationMember(
                        organization,
                        user,
                        OrganizationRole.OWNER
                );

        Customer customer =
                new Customer(
                        organization,
                        "Cliente"
                );

        opportunity =
                new Opportunity(
                        organization,
                        customer,
                        "Oportunidad"
                );

        ReflectionTestUtils.setField(
                opportunity,
                "id",
                opportunityId
        );

        when(
                accessService.requireMember(
                        organizationId,
                        userId
                )
        ).thenReturn(currentMember);
    }

    @Test
    void pendingFollowUpCanBeCompleted() {

        FollowUp followUp = createFollowUp();

        when(
                followUpRepository
                        .findByIdAndOrganization_Id(
                                followUpId,
                                organizationId
                        )
        ).thenReturn(Optional.of(followUp));

        followUpService.complete(
                organizationId,
                followUpId,
                userId
        );

        assertThat(followUp.getStatus())
                .isEqualTo(FollowUpStatus.COMPLETED);

        assertThat(followUp.getCompletedAt())
                .isEqualTo(fixedInstant);

        verify(activityRepository)
                .save(any());
    }

    @Test
    void completedFollowUpCannotBeCompletedAgain() {

        FollowUp followUp = createFollowUp();

        followUp.complete(
                fixedInstant.minusSeconds(60)
        );

        when(
                followUpRepository
                        .findByIdAndOrganization_Id(
                                followUpId,
                                organizationId
                        )
        ).thenReturn(Optional.of(followUp));

        assertThatThrownBy(() ->
                followUpService.complete(
                        organizationId,
                        followUpId,
                        userId
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException =
                            (ApiException) exception;

                    assertThat(
                            apiException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.FOLLOW_UP_NOT_COMPLETABLE
                    );
                });

        verify(activityRepository, never())
                .save(any());
    }

    @Test
    void completedFollowUpCannotBeEdited() {

        FollowUp followUp = createFollowUp();

        followUp.complete(fixedInstant);

        when(
                followUpRepository
                        .findByIdAndOrganization_Id(
                                followUpId,
                                organizationId
                        )
        ).thenReturn(Optional.of(followUp));

        UpdateFollowUpRequest request =
                new UpdateFollowUpRequest(
                        FollowUpType.EMAIL,
                        null,
                        null,
                        null,
                        null
                );

        assertThatThrownBy(() ->
                followUpService.update(
                        organizationId,
                        followUpId,
                        userId,
                        request
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException =
                            (ApiException) exception;

                    assertThat(
                            apiException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.FOLLOW_UP_NOT_EDITABLE
                    );
                });
    }

    @Test
    void pendingFollowUpCanBeCancelled() {

        FollowUp followUp = createFollowUp();

        when(
                followUpRepository
                        .findByIdAndOrganization_Id(
                                followUpId,
                                organizationId
                        )
        ).thenReturn(Optional.of(followUp));

        followUpService.cancel(
                organizationId,
                followUpId,
                userId
        );

        assertThat(followUp.getStatus())
                .isEqualTo(FollowUpStatus.CANCELLED);

        assertThat(followUp.getCompletedAt())
                .isNull();
    }

    @Test
    void inactiveMemberCannotBeAssigned() {

        User assignedUser =
                new User(
                        "assigned@test.com",
                        "hash",
                        "Assigned",
                        "User"
                );

        OrganizationMember assignedMember =
                new OrganizationMember(
                        organization,
                        assignedUser,
                        OrganizationRole.MEMBER
                );

        ReflectionTestUtils.setField(
                assignedMember,
                "id",
                assignedMemberId
        );

        assignedMember.deactivate();

        when(
                opportunityRepository
                        .findByIdAndOrganization_Id(
                                opportunityId,
                                organizationId
                        )
        ).thenReturn(Optional.of(opportunity));

        when(
                memberRepository
                        .findByIdAndOrganization_Id(
                                assignedMemberId,
                                organizationId
                        )
        ).thenReturn(Optional.of(assignedMember));

        CreateFollowUpRequest request =
                new CreateFollowUpRequest(
                        FollowUpType.CALL,
                        fixedInstant.plusSeconds(3600),
                        assignedMemberId,
                        "Llamar al cliente"
                );

        assertThatThrownBy(() ->
                followUpService.create(
                        organizationId,
                        opportunityId,
                        userId,
                        request
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException =
                            (ApiException) exception;

                    assertThat(
                            apiException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.ASSIGNED_MEMBER_INACTIVE
                    );
                });

        verify(followUpRepository, never())
                .save(any());
    }

    @Test
    void followUpCannotBeCreatedForClosedOpportunity() {

        opportunity.changeStatus(
                OpportunityStatus.WON,
                null,
                fixedInstant
        );

        when(
                opportunityRepository
                        .findByIdAndOrganization_Id(
                                opportunityId,
                                organizationId
                        )
        ).thenReturn(Optional.of(opportunity));

        CreateFollowUpRequest request =
                new CreateFollowUpRequest(
                        FollowUpType.CALL,
                        fixedInstant.plusSeconds(3600),
                        null,
                        null
                );

        assertThatThrownBy(() ->
                followUpService.create(
                        organizationId,
                        opportunityId,
                        userId,
                        request
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException =
                            (ApiException) exception;

                    assertThat(
                            apiException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.OPPORTUNITY_CLOSED
                    );
                });

        verify(followUpRepository, never())
                .save(any());
    }

    private FollowUp createFollowUp() {

        FollowUp followUp =
                new FollowUp(
                        organization,
                        opportunity,
                        FollowUpType.CALL,
                        fixedInstant.plusSeconds(3600)
                );

        ReflectionTestUtils.setField(
                followUp,
                "id",
                followUpId
        );

        return followUp;
    }
}