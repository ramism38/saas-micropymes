package com.micropymes.backend.quote.domain;

import com.micropymes.backend.customer.domain.Customer;
import com.micropymes.backend.opportunity.domain.Opportunity;
import com.micropymes.backend.organization.domain.Organization;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class QuoteTest {

    @Test
    void newQuoteStartsAsDraft() {

        Quote quote = createQuote();

        assertThat(quote.getStatus())
                .isEqualTo(QuoteStatus.DRAFT);

        assertThat(quote.getSentAt())
                .isNull();
    }

    @Test
    void sendingQuoteChangesStatusAndStoresDate() {

        Quote quote = createQuote();

        Instant now =
                Instant.parse("2026-09-12T10:00:00Z");

        quote.send(now);

        assertThat(quote.getStatus())
                .isEqualTo(QuoteStatus.SENT);

        assertThat(quote.getSentAt())
                .isEqualTo(now);
    }

    @Test
    void quoteCanBeAccepted() {

        Quote quote = createQuote();

        quote.accept();

        assertThat(quote.getStatus())
                .isEqualTo(QuoteStatus.ACCEPTED);
    }

    @Test
    void quoteCanBeRejected() {

        Quote quote = createQuote();

        quote.reject();

        assertThat(quote.getStatus())
                .isEqualTo(QuoteStatus.REJECTED);
    }

    @Test
    void quoteCanExpire() {

        Quote quote = createQuote();

        quote.expire();

        assertThat(quote.getStatus())
                .isEqualTo(QuoteStatus.EXPIRED);
    }

    private Quote createQuote() {

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

        return new Quote(
                organization,
                opportunity,
                new BigDecimal("1200.00"),
                "EUR"
        );
    }
}