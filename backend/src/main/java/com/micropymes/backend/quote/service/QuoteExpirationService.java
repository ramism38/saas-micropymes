package com.micropymes.backend.quote.service;

import com.micropymes.backend.activity.domain.Activity;
import com.micropymes.backend.activity.domain.ActivityType;
import com.micropymes.backend.activity.repository.ActivityRepository;
import com.micropymes.backend.quote.domain.Quote;
import com.micropymes.backend.quote.domain.QuoteStatus;
import com.micropymes.backend.quote.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class QuoteExpirationService {

    private final QuoteRepository quoteRepository;
    private final ActivityRepository activityRepository;
    private final Clock clock;

    public QuoteExpirationService(
            QuoteRepository quoteRepository,
            ActivityRepository activityRepository,
            Clock clock
    ) {
        this.quoteRepository = quoteRepository;
        this.activityRepository = activityRepository;
        this.clock = clock;
    }

    @Transactional
    public int expireDueQuotes() {

        Instant now = Instant.now(clock);

        List<Quote> quotes =
                quoteRepository
                        .findByStatusAndExpiresAtIsNotNullAndExpiresAtLessThanEqual(
                                QuoteStatus.SENT,
                                now
                        );

        for (Quote quote : quotes) {

            quote.expire();

            activityRepository.save(
                    new Activity(
                            quote.getOrganization(),
                            quote.getOpportunity(),
                            null,
                            ActivityType.QUOTE_EXPIRED,
                            "Quote expired"
                    )
            );
        }

        return quotes.size();
    }
}