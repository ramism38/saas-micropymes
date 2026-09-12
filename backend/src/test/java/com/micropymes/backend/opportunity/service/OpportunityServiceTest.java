package com.micropymes.backend.opportunity.service;

import com.micropymes.backend.activity.repository.ActivityRepository;
import com.micropymes.backend.common.error.ApiException;
import com.micropymes.backend.common.error.ErrorCode;
import com.micropymes.backend.customer.domain.Customer;
import com.micropymes.backend.customer.repository.CustomerRepository;
import com.micropymes.backend.followup.domain.FollowUp;
import com.micropymes.backend.followup.domain.FollowUpStatus;
import com.micropymes.backend.followup.domain.FollowUpType;
import com.micropymes.backend.followup.repository.FollowUpRepository;
import com.micropymes.backend.opportunity.domain.Opportunity;
import com.micropymes.backend.opportunity.domain.OpportunityStatus;
import com.micropymes.backend.opportunity.dto.ChangeOpportunityStatusRequest;
import com.micropymes.backend.opportunity.dto.UpdateOpportunityRequest;
import com.micropymes.backend.opportunity.repository.OpportunityRepository;
import com.micropymes.backend.organization.domain.Organization;
import com.micropymes.backend.organization.domain.OrganizationMember;
import com.micropymes.backend.organization.domain.OrganizationRole;
import com.micropymes.backend.organization.service.OrganizationAccessService;
import com.micropymes.backend.quote.domain.QuoteStatus;
import com.micropymes.backend.quote.repository.QuoteRepository;
import com.micropymes.backend.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OpportunityServiceTest {

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private FollowUpRepository followUpRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private OrganizationAccessService accessService;

    private OpportunityService opportunityService;

    private Organization organization;
    private User user;
    private OrganizationMember member;
    private Customer customer;

    private final UUID organizationId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID opportunityId = UUID.randomUUID();

    private final Instant fixedInstant = Instant.parse("2026-09-12T10:00:00Z");

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        Clock clock = Clock.fixed(
                fixedInstant,
                ZoneOffset.UTC);

        opportunityService = new OpportunityService(
                opportunityRepository,
                customerRepository,
                activityRepository,
                followUpRepository,
                quoteRepository,
                accessService,
                clock);

        organization = new Organization(
                "Empresa",
                "EUR");

        user = new User(
                "user@test.com",
                "hash",
                "Test",
                "User");

        member = new OrganizationMember(
                organization,
                user,
                OrganizationRole.OWNER);

        customer = new Customer(
                organization,
                "Cliente");

        when(
                accessService.requireMember(
                        organizationId,
                        userId))
                .thenReturn(member);
    }

    @Test
    void closedOpportunityCannotBeEdited() {

        Opportunity opportunity = createOpportunity();

        opportunity.changeStatus(
                OpportunityStatus.WON,
                null,
                fixedInstant);

        when(
                opportunityRepository
                        .findByIdAndOrganization_Id(
                                opportunityId,
                                organizationId))
                .thenReturn(Optional.of(opportunity));

        UpdateOpportunityRequest request = new UpdateOpportunityRequest(
                "Nuevo título",
                null,
                null,
                null);

        assertThatThrownBy(() -> opportunityService.update(
                organizationId,
                opportunityId,
                userId,
                request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException = (ApiException) exception;

                    assertThat(
                            apiException.getErrorCode()).isEqualTo(
                                    ErrorCode.OPPORTUNITY_CLOSED);
                });
    }

    @Test
    void winningOpportunityCancelsPendingFollowUps() {

        Opportunity opportunity = createOpportunity();

        FollowUp followUp = new FollowUp(
                organization,
                opportunity,
                FollowUpType.CALL,
                fixedInstant.plusSeconds(3600));

        when(
                opportunityRepository
                        .findByIdAndOrganization_Id(
                                opportunityId,
                                organizationId))
                .thenReturn(Optional.of(opportunity));

        when(
                followUpRepository
                        .findByOpportunity_IdAndOrganization_IdAndStatus(
                                opportunityId,
                                organizationId,
                                FollowUpStatus.PENDING))
                .thenReturn(List.of(followUp));

        ChangeOpportunityStatusRequest request = new ChangeOpportunityStatusRequest(
                OpportunityStatus.WON,
                null);

        opportunityService.changeStatus(
                organizationId,
                opportunityId,
                userId,
                request);

        assertThat(opportunity.getStatus())
                .isEqualTo(OpportunityStatus.WON);

        assertThat(opportunity.getClosedAt())
                .isEqualTo(fixedInstant);

        assertThat(followUp.getStatus())
                .isEqualTo(FollowUpStatus.CANCELLED);

        verify(activityRepository)
                .save(any());
    }

    @Test
    void lostOpportunityStoresLostReason() {

        Opportunity opportunity = createOpportunity();

        when(
                opportunityRepository
                        .findByIdAndOrganization_Id(
                                opportunityId,
                                organizationId))
                .thenReturn(Optional.of(opportunity));

        when(
                followUpRepository
                        .findByOpportunity_IdAndOrganization_IdAndStatus(
                                opportunityId,
                                organizationId,
                                FollowUpStatus.PENDING))
                .thenReturn(List.of());

        ChangeOpportunityStatusRequest request = new ChangeOpportunityStatusRequest(
                OpportunityStatus.LOST,
                "Cliente no interesado");

        opportunityService.changeStatus(
                organizationId,
                opportunityId,
                userId,
                request);

        assertThat(opportunity.getStatus())
                .isEqualTo(OpportunityStatus.LOST);

        assertThat(opportunity.getLostReason())
                .isEqualTo("Cliente no interesado");

        assertThat(opportunity.getClosedAt())
                .isEqualTo(fixedInstant);
    }

    @Test
    void wonOpportunityWithAcceptedQuoteCannotBeReopened() {

        Opportunity opportunity = createOpportunity();

        opportunity.changeStatus(
                OpportunityStatus.WON,
                null,
                fixedInstant);

        when(
                opportunityRepository
                        .findByIdAndOrganization_Id(
                                opportunityId,
                                organizationId))
                .thenReturn(Optional.of(opportunity));

        when(
                quoteRepository
                        .existsByOpportunity_IdAndStatus(
                                opportunityId,
                                QuoteStatus.ACCEPTED))
                .thenReturn(true);

        ChangeOpportunityStatusRequest request = new ChangeOpportunityStatusRequest(
                OpportunityStatus.CONTACTED,
                null);

        assertThatThrownBy(() -> opportunityService.changeStatus(
                organizationId,
                opportunityId,
                userId,
                request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException = (ApiException) exception;

                    assertThat(
                            apiException.getErrorCode()).isEqualTo(
                                    ErrorCode.OPPORTUNITY_HAS_ACCEPTED_QUOTE);
                });
    }

    @Test
    void archivedOpportunityCannotBeEdited() {

        Opportunity opportunity = createOpportunity();

        opportunity.archive();

        when(
                opportunityRepository
                        .findByIdAndOrganization_Id(
                                opportunityId,
                                organizationId))
                .thenReturn(Optional.of(opportunity));

        UpdateOpportunityRequest request = new UpdateOpportunityRequest(
                "Nuevo título",
                null,
                null,
                null);

        assertThatThrownBy(() -> opportunityService.update(
                organizationId,
                opportunityId,
                userId,
                request))
                .isInstanceOf(ApiException.class)
                .satisfies(exception -> {

                    ApiException apiException = (ApiException) exception;

                    assertThat(
                            apiException.getErrorCode()).isEqualTo(
                                    ErrorCode.OPPORTUNITY_ARCHIVED);
                });
    }

    private Opportunity createOpportunity() {

        return new Opportunity(
                organization,
                customer,
                "Oportunidad");
    }
}