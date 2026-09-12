package com.micropymes.backend.quote.scheduler;

import com.micropymes.backend.quote.service.QuoteExpirationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class QuoteExpirationScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    QuoteExpirationScheduler.class
            );

    private final QuoteExpirationService expirationService;

    public QuoteExpirationScheduler(
            QuoteExpirationService expirationService
    ) {
        this.expirationService = expirationService;
    }

    @Scheduled(
            cron = "0 0 * * * *",
            zone = "UTC"
    )
    public void expireQuotes() {

        int expired =
                expirationService.expireDueQuotes();

        if (expired > 0) {
            log.info(
                    "Expired {} quote(s)",
                    expired
            );
        }
    }
}