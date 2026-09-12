package com.micropymes.backend.followup.domain;

import com.micropymes.backend.customer.domain.Customer;
import com.micropymes.backend.opportunity.domain.Opportunity;
import com.micropymes.backend.organization.domain.Organization;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class FollowUpTest {

    @Test
    void newFollowUpStartsPending() {

        FollowUp followUp = createFollowUp();

        assertThat(followUp.getStatus())
                .isEqualTo(FollowUpStatus.PENDING);

        assertThat(followUp.getCompletedAt())
                .isNull();
    }

    @Test
    void completingFollowUpStoresCompletionDate() {

        FollowUp followUp = createFollowUp();

        Instant now =
                Instant.parse("2026-09-12T12:00:00Z");

        followUp.complete(now);

        assertThat(followUp.getStatus())
                .isEqualTo(FollowUpStatus.COMPLETED);

        assertThat(followUp.getCompletedAt())
                .isEqualTo(now);
    }

    @Test
    void cancellingFollowUpChangesStatus() {

        FollowUp followUp = createFollowUp();

        followUp.cancel();

        assertThat(followUp.getStatus())
                .isEqualTo(FollowUpStatus.CANCELLED);

        assertThat(followUp.getCompletedAt())
                .isNull();
    }

    private FollowUp createFollowUp() {

        Organization organization =
                new Organization("Empresa", "EUR");

        Customer customer =
                new Customer(organization, "Cliente");

        Opportunity opportunity =
                new Opportunity(
                        organization,
                        customer,
                        "Oportunidad"
                );

        return new FollowUp(
                organization,
                opportunity,
                FollowUpType.CALL,
                Instant.parse("2026-09-15T10:00:00Z")
        );
    }
}