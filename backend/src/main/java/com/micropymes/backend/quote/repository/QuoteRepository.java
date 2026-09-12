package com.micropymes.backend.quote.repository;

import com.micropymes.backend.quote.domain.Quote;
import com.micropymes.backend.quote.domain.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuoteRepository extends JpaRepository<Quote, UUID> {

        Optional<Quote> findByIdAndOrganization_Id(
                        UUID quoteId,
                        UUID organizationId);

        List<Quote> findByOpportunity_IdAndOrganization_Id(
                        UUID opportunityId,
                        UUID organizationId);

        boolean existsByOpportunity_IdAndStatus(
                        UUID opportunityId,
                        QuoteStatus status);

        List<Quote> findByOpportunity_IdAndOrganization_IdOrderByCreatedAtDesc(
                        UUID opportunityId,
                        UUID organizationId);

        Optional<Quote> findFirstByOpportunity_IdAndOrganization_IdAndStatusOrderBySentAtDesc(
                        UUID opportunityId,
                        UUID organizationId,
                        QuoteStatus status);
}