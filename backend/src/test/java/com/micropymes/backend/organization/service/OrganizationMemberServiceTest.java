package com.micropymes.backend.organization.service;

import com.micropymes.backend.common.error.ApiException;
import com.micropymes.backend.common.error.ErrorCode;
import com.micropymes.backend.followup.domain.FollowUp;
import com.micropymes.backend.followup.domain.FollowUpStatus;
import com.micropymes.backend.followup.domain.FollowUpType;
import com.micropymes.backend.followup.repository.FollowUpRepository;
import com.micropymes.backend.opportunity.domain.Opportunity;
import com.micropymes.backend.customer.domain.Customer;
import com.micropymes.backend.organization.domain.Organization;
import com.micropymes.backend.organization.domain.OrganizationMember;
import com.micropymes.backend.organization.domain.OrganizationRole;
import com.micropymes.backend.organization.dto.AddMemberRequest;
import com.micropymes.backend.organization.dto.UpdateMemberRoleRequest;
import com.micropymes.backend.organization.repository.OrganizationMemberRepository;
import com.micropymes.backend.user.domain.User;
import com.micropymes.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OrganizationMemberServiceTest {

    @Mock
    private OrganizationMemberRepository memberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowUpRepository followUpRepository;

    @Mock
    private OrganizationAccessService accessService;

    private OrganizationMemberService memberService;

    private Organization organization;
    private OrganizationMember currentOwner;

    private final UUID organizationId = UUID.randomUUID();
    private final UUID currentUserId = UUID.randomUUID();
    private final UUID targetUserId = UUID.randomUUID();
    private final UUID targetMemberId = UUID.randomUUID();

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        memberService = new OrganizationMemberService(
                memberRepository,
                userRepository,
                followUpRepository,
                accessService
        );

        organization =
                new Organization(
                        "Empresa",
                        "EUR"
                );

        User currentUser =
                new User(
                        "owner@test.com",
                        "hash",
                        "Owner",
                        "User"
                );

        ReflectionTestUtils.setField(
                currentUser,
                "id",
                currentUserId
        );

        currentOwner =
                new OrganizationMember(
                        organization,
                        currentUser,
                        OrganizationRole.OWNER
                );

        when(
                accessService.requireOwner(
                        organizationId,
                        currentUserId
                )
        ).thenReturn(currentOwner);
    }

    @Test
    void lastOwnerCannotBeDemoted() {

        OrganizationMember target =
                createTargetMember(
                        OrganizationRole.OWNER
                );

        when(
                memberRepository
                        .findByIdAndOrganization_Id(
                                targetMemberId,
                                organizationId
                        )
        ).thenReturn(Optional.of(target));

        when(
                memberRepository
                        .countByOrganization_IdAndRoleAndActiveTrue(
                                organizationId,
                                OrganizationRole.OWNER
                        )
        ).thenReturn(1L);

        UpdateMemberRoleRequest request =
                new UpdateMemberRoleRequest(
                        OrganizationRole.MEMBER
                );

        assertThatThrownBy(() ->
                memberService.changeRole(
                        organizationId,
                        targetMemberId,
                        currentUserId,
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
                            ErrorCode.LAST_OWNER_REQUIRED
                    );
                });

        assertThat(target.getRole())
                .isEqualTo(OrganizationRole.OWNER);
    }

    @Test
    void ownerCanBeDemotedWhenAnotherOwnerExists() {

        OrganizationMember target =
                createTargetMember(
                        OrganizationRole.OWNER
                );

        when(
                memberRepository
                        .findByIdAndOrganization_Id(
                                targetMemberId,
                                organizationId
                        )
        ).thenReturn(Optional.of(target));

        when(
                memberRepository
                        .countByOrganization_IdAndRoleAndActiveTrue(
                                organizationId,
                                OrganizationRole.OWNER
                        )
        ).thenReturn(2L);

        memberService.changeRole(
                organizationId,
                targetMemberId,
                currentUserId,
                new UpdateMemberRoleRequest(
                        OrganizationRole.MEMBER
                )
        );

        assertThat(target.getRole())
                .isEqualTo(OrganizationRole.MEMBER);
    }

    @Test
    void lastOwnerCannotBeDeactivated() {

        OrganizationMember target =
                createTargetMember(
                        OrganizationRole.OWNER
                );

        when(
                memberRepository
                        .findByIdAndOrganization_Id(
                                targetMemberId,
                                organizationId
                        )
        ).thenReturn(Optional.of(target));

        when(
                memberRepository
                        .countByOrganization_IdAndRoleAndActiveTrue(
                                organizationId,
                                OrganizationRole.OWNER
                        )
        ).thenReturn(1L);

        assertThatThrownBy(() ->
                memberService.deactivate(
                        organizationId,
                        targetMemberId,
                        currentUserId
                )
        )
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException =
                            (ApiException) exception;

                    assertThat(
                            apiException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.LAST_OWNER_REQUIRED
                    );
                });

        assertThat(target.isActive()).isTrue();
    }

    @Test
    void deactivatingMemberUnassignsPendingFollowUps() {

        OrganizationMember target =
                createTargetMember(
                        OrganizationRole.MEMBER
                );

        Customer customer =
                new Customer(
                        organization,
                        "Cliente"
                );

        Opportunity opportunity =
                new Opportunity(
                        organization,
                        customer,
                        "Oportunidad"
                );

        FollowUp followUp =
                new FollowUp(
                        organization,
                        opportunity,
                        FollowUpType.CALL,
                        Instant.parse(
                                "2026-09-15T10:00:00Z"
                        )
                );

        followUp.setAssignedToMember(target);

        when(
                memberRepository
                        .findByIdAndOrganization_Id(
                                targetMemberId,
                                organizationId
                        )
        ).thenReturn(Optional.of(target));

        when(
                followUpRepository
                        .findByOrganization_IdAndAssignedToMember_IdAndStatus(
                                organizationId,
                                targetMemberId,
                                FollowUpStatus.PENDING
                        )
        ).thenReturn(List.of(followUp));

        memberService.deactivate(
                organizationId,
                targetMemberId,
                currentUserId
        );

        assertThat(target.isActive()).isFalse();
        assertThat(target.getLeftAt()).isNotNull();

        assertThat(
                followUp.getAssignedToMember()
        ).isNull();
    }

    @Test
    void activeMemberCannotBeAddedTwice() {

        User targetUser = createTargetUser();

        OrganizationMember existing =
                new OrganizationMember(
                        organization,
                        targetUser,
                        OrganizationRole.MEMBER
                );

        when(
                userRepository.findByEmailIgnoreCase(
                        "member@test.com"
                )
        ).thenReturn(Optional.of(targetUser));

        when(
                memberRepository
                        .findByOrganization_IdAndUser_Id(
                                organizationId,
                                targetUserId
                        )
        ).thenReturn(Optional.of(existing));

        AddMemberRequest request =
                new AddMemberRequest(
                        "member@test.com",
                        OrganizationRole.MEMBER
                );

        assertThatThrownBy(() ->
                memberService.addMember(
                        organizationId,
                        currentUserId,
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
                            ErrorCode.MEMBER_ALREADY_EXISTS
                    );
                });
    }

    @Test
    void inactiveMemberCanBeReactivated() {

        User targetUser = createTargetUser();

        OrganizationMember existing =
                new OrganizationMember(
                        organization,
                        targetUser,
                        OrganizationRole.MEMBER
                );

        existing.deactivate();

        when(
                userRepository.findByEmailIgnoreCase(
                        "member@test.com"
                )
        ).thenReturn(Optional.of(targetUser));

        when(
                memberRepository
                        .findByOrganization_IdAndUser_Id(
                                organizationId,
                                targetUserId
                        )
        ).thenReturn(Optional.of(existing));

        memberService.addMember(
                organizationId,
                currentUserId,
                new AddMemberRequest(
                        "member@test.com",
                        OrganizationRole.OWNER
                )
        );

        assertThat(existing.isActive())
                .isTrue();

        assertThat(existing.getLeftAt())
                .isNull();

        assertThat(existing.getRole())
                .isEqualTo(OrganizationRole.OWNER);
    }

    private OrganizationMember createTargetMember(
            OrganizationRole role
    ) {

        User user = createTargetUser();

        OrganizationMember member =
                new OrganizationMember(
                        organization,
                        user,
                        role
                );

        ReflectionTestUtils.setField(
                member,
                "id",
                targetMemberId
        );

        return member;
    }

    private User createTargetUser() {

        User user =
                new User(
                        "member@test.com",
                        "hash",
                        "Member",
                        "User"
                );

        ReflectionTestUtils.setField(
                user,
                "id",
                targetUserId
        );

        return user;
    }
}