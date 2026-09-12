package com.micropymes.backend.quote.controller;

import com.micropymes.backend.auth.security.AuthenticatedUser;
import com.micropymes.backend.quote.dto.CreateQuoteRequest;
import com.micropymes.backend.quote.dto.QuoteResponse;
import com.micropymes.backend.quote.dto.UpdateQuoteRequest;
import com.micropymes.backend.quote.service.QuoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}")
@Tag(name = "Quotes", description = "Quote management")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(
            QuoteService quoteService
    ) {
        this.quoteService = quoteService;
    }

    @PostMapping(
            "/opportunities/{opportunityId}/quotes"
    )
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteResponse create(
            @PathVariable UUID organizationId,
            @PathVariable UUID opportunityId,
            @Valid
            @RequestBody CreateQuoteRequest request,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return quoteService.create(
                organizationId,
                opportunityId,
                principal.userId(),
                request
        );
    }

    @GetMapping(
            "/opportunities/{opportunityId}/quotes"
    )
    public List<QuoteResponse> findAll(
            @PathVariable UUID organizationId,
            @PathVariable UUID opportunityId,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return quoteService.findAll(
                organizationId,
                opportunityId,
                principal.userId()
        );
    }

    @GetMapping("/quotes/{quoteId}")
    public QuoteResponse findById(
            @PathVariable UUID organizationId,
            @PathVariable UUID quoteId,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return quoteService.findById(
                organizationId,
                quoteId,
                principal.userId()
        );
    }

    @PatchMapping("/quotes/{quoteId}")
    public QuoteResponse update(
            @PathVariable UUID organizationId,
            @PathVariable UUID quoteId,
            @Valid
            @RequestBody UpdateQuoteRequest request,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return quoteService.update(
                organizationId,
                quoteId,
                principal.userId(),
                request
        );
    }

    @PostMapping("/quotes/{quoteId}/send")
    public QuoteResponse send(
            @PathVariable UUID organizationId,
            @PathVariable UUID quoteId,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return quoteService.send(
                organizationId,
                quoteId,
                principal.userId()
        );
    }

    @PostMapping("/quotes/{quoteId}/accept")
    public QuoteResponse accept(
            @PathVariable UUID organizationId,
            @PathVariable UUID quoteId,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return quoteService.accept(
                organizationId,
                quoteId,
                principal.userId()
        );
    }

    @PostMapping("/quotes/{quoteId}/reject")
    public QuoteResponse reject(
            @PathVariable UUID organizationId,
            @PathVariable UUID quoteId,
            @AuthenticationPrincipal
            AuthenticatedUser principal
    ) {
        return quoteService.reject(
                organizationId,
                quoteId,
                principal.userId()
        );
    }
}