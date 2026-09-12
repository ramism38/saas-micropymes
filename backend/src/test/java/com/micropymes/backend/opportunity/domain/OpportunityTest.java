package com.micropymes.backend.opportunity.domain;

import com.micropymes.backend.customer.domain.Customer;
import com.micropymes.backend.organization.domain.Organization;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class OpportunityTest {

    @Test
    void newOpportunityStartsOpenAndNew() {

        Organization organization =
                new Organization("Empresa", "EUR");

        Customer customer =
                new Customer(organization, "Cliente");

        Opportunity opportunity =
                new Opportunity(
                        organization,
                        customer,
                        "Nueva oportunidad"
                );

        assertThat(opportunity.getStatus())
                .isEqualTo(OpportunityStatus.NEW);

        assertThat(opportunity.isClosed())
                .isFalse();

        assertThat(opportunity.getClosedAt())
                .isNull();
    }

    @Test
    void winningOpportunityClosesIt() {

        Opportunity opportunity = createOpportunity();

        Instant now =
                Instant.parse("2026-09-12T10:00:00Z");

        opportunity.changeStatus(
                OpportunityStatus.WON,
                null,
                now
        );

        assertThat(opportunity.getStatus())
                .isEqualTo(OpportunityStatus.WON);

        assertThat(opportunity.isClosed())
                .isTrue();

        assertThat(opportunity.getClosedAt())
                .isEqualTo(now);

        assertThat(opportunity.getLostReason())
                .isNull();
    }

    @Test
    void losingOpportunityStoresReason() {

        Opportunity opportunity = createOpportunity();

        Instant now =
                Instant.parse("2026-09-12T10:00:00Z");

        opportunity.changeStatus(
                OpportunityStatus.LOST,
                "Price too high",
                now
        );

        assertThat(opportunity.getStatus())
                .isEqualTo(OpportunityStatus.LOST);

        assertThat(opportunity.getClosedAt())
                .isEqualTo(now);

        assertThat(opportunity.getLostReason())
                .isEqualTo("Price too high");
    }

    @Test
    void reopeningOpportunityClearsClosingData() {

        Opportunity opportunity = createOpportunity();

        opportunity.changeStatus(
                OpportunityStatus.LOST,
                "No budget",
                Instant.parse("2026-09-12T10:00:00Z")
        );

        opportunity.changeStatus(
                OpportunityStatus.CONTACTED,
                null,
                Instant.parse("2026-09-13T10:00:00Z")
        );

        assertThat(opportunity.getStatus())
                .isEqualTo(OpportunityStatus.CONTACTED);

        assertThat(opportunity.isClosed())
                .isFalse();

        assertThat(opportunity.getClosedAt())
                .isNull();

        assertThat(opportunity.getLostReason())
                .isNull();
    }

    @Test
    void opportunityCanBeArchivedAndRestored() {

        Opportunity opportunity = createOpportunity();

        opportunity.archive();

        assertThat(opportunity.isArchived())
                .isTrue();

        opportunity.restore();

        assertThat(opportunity.isArchived())
                .isFalse();
    }

    private Opportunity createOpportunity() {

        Organization organization =
                new Organization("Empresa", "EUR");

        Customer customer =
                new Customer(organization, "Cliente");

        return new Opportunity(
                organization,
                customer,
                "Oportunidad"
        );
    }
}