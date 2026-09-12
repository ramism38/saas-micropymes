package com.micropymes.backend.quote.service;

import com.micropymes.backend.activity.repository.ActivityRepository;
import com.micropymes.backend.common.error.ApiException;
import com.micropymes.backend.common.error.ErrorCode;
import com.micropymes.backend.customer.domain.Customer;
import com.micropymes.backend.followup.domain.FollowUp;
import com.micropymes.backend.followup.domain.FollowUpStatus;
import com.micropymes.backend.followup.domain.FollowUpType;
import com.micropymes.backend.followup.repository.FollowUpRepository;
import com.micropymes.backend.opportunity.domain.Opportunity;
import com.micropymes.backend.opportunity.domain.OpportunityStatus;
import com.micropymes.backend.opportunity.repository.OpportunityRepository;
import com.micropymes.backend.organization.domain.Organization;
import com.micropymes.backend.organization.domain.OrganizationMember;
import com.micropymes.backend.organization.domain.OrganizationRole;
import com.micropymes.backend.organization.service.OrganizationAccessService;
import com.micropymes.backend.quote.domain.Quote;
import com.micropymes.backend.quote.domain.QuoteStatus;
import com.micropymes.backend.quote.dto.UpdateQuoteRequest;
import com.micropymes.backend.quote.repository.QuoteRepository;
import com.micropymes.backend.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
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

class QuoteServiceTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private FollowUpRepository followUpRepository;

    @Mock
    private OrganizationAccessService accessService;

    private QuoteService quoteService;

    private Organization organization;
    private OrganizationMember member;
    private Opportunity opportunity;

    private final UUID organizationId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID opportunityId = UUID.randomUUID();
    private final UUID quoteId = UUID.randomUUID();

    private final Instant fixedInstant =
            Instant.parse("2026-09-12T10:00:00Z");

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        Clock clock = Clock.fixed(
                fixedInstant,
                ZoneOffset.UTC
        );

        quoteService = new QuoteService(
                quoteRepository,
                opportunityRepository,
                activityRepository,
                followUpRepository,
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

        member =
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

        /*
         * Como estas entidades no pasan por Hibernate
         * durante el test unitario, asignamos manualmente
         * los UUID necesarios.
         */
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
        ).thenReturn(member);
    }

    @Test
    void draftQuoteCannotBeAccepted() {

        Quote quote = createQuote();

        when(
                quoteRepository
                        .findByIdAndOrganization_Id(
                                quoteId,
                                organizationId
                        )
        ).thenReturn(Optional.of(quote));

        assertThatThrownBy(() ->
                quoteService.accept(
                        organizationId,
                        quoteId,
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
                            ErrorCode.QUOTE_NOT_ACCEPTABLE
                    );
                });
    }

    @Test
    void sendingQuoteChangesQuoteToSentAndOpportunityToProposalSent() {

        Quote quote = createQuote();

        when(
                quoteRepository
                        .findByIdAndOrganization_Id(
                                quoteId,
                                organizationId
                        )
        ).thenReturn(Optional.of(quote));

        quoteService.send(
                organizationId,
                quoteId,
                userId
        );

        assertThat(quote.getStatus())
                .isEqualTo(QuoteStatus.SENT);

        assertThat(quote.getSentAt())
                .isEqualTo(fixedInstant);

        assertThat(opportunity.getStatus())
                .isEqualTo(
                        OpportunityStatus.PROPOSAL_SENT
                );

        verify(
                activityRepository,
                times(2)
        ).save(any());
    }

    @Test
    void acceptingQuoteWinsOpportunityAndCancelsPendingFollowUps() {

        Quote quote = createQuote();

        quote.send(
                fixedInstant.minusSeconds(3600)
        );

        FollowUp followUp =
                new FollowUp(
                        organization,
                        opportunity,
                        FollowUpType.CALL,
                        fixedInstant.plusSeconds(3600)
                );

        when(
                quoteRepository
                        .findByIdAndOrganization_Id(
                                quoteId,
                                organizationId
                        )
        ).thenReturn(Optional.of(quote));

        when(
                quoteRepository
                        .existsByOpportunity_IdAndStatus(
                                opportunityId,
                                QuoteStatus.ACCEPTED
                        )
        ).thenReturn(false);

        when(
                followUpRepository
                        .findByOpportunity_IdAndOrganization_IdAndStatus(
                                opportunityId,
                                organizationId,
                                FollowUpStatus.PENDING
                        )
        ).thenReturn(List.of(followUp));

        quoteService.accept(
                organizationId,
                quoteId,
                userId
        );

        assertThat(quote.getStatus())
                .isEqualTo(QuoteStatus.ACCEPTED);

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
    void secondAcceptedQuoteIsRejected() {

        Quote quote = createQuote();

        quote.send(
                fixedInstant.minusSeconds(3600)
        );

        when(
                quoteRepository
                        .findByIdAndOrganization_Id(
                                quoteId,
                                organizationId
                        )
        ).thenReturn(Optional.of(quote));

        when(
                quoteRepository
                        .existsByOpportunity_IdAndStatus(
                                opportunityId,
                                QuoteStatus.ACCEPTED
                        )
        ).thenReturn(true);

        assertThatThrownBy(() ->
                quoteService.accept(
                        organizationId,
                        quoteId,
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
                            ErrorCode.QUOTE_ALREADY_ACCEPTED
                    );
                });

        assertThat(quote.getStatus())
                .isEqualTo(QuoteStatus.SENT);

        assertThat(opportunity.getStatus())
                .isEqualTo(OpportunityStatus.NEW);
    }

    @Test
    void sentQuoteCannotBeEdited() {

        Quote quote = createQuote();

        quote.send(fixedInstant);

        when(
                quoteRepository
                        .findByIdAndOrganization_Id(
                                quoteId,
                                organizationId
                        )
        ).thenReturn(Optional.of(quote));

        UpdateQuoteRequest request =
                new UpdateQuoteRequest(
                        new BigDecimal("2500.00"),
                        null,
                        null,
                        null
                );

        assertThatThrownBy(() ->
                quoteService.update(
                        organizationId,
                        quoteId,
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
                            ErrorCode.QUOTE_NOT_EDITABLE
                    );
                });
    }

    private Quote createQuote() {

        Quote quote =
                new Quote(
                        organization,
                        opportunity,
                        new BigDecimal("1500.00"),
                        "EUR"
                );

        ReflectionTestUtils.setField(
                quote,
                "id",
                quoteId
        );

        return quote;
    }
}