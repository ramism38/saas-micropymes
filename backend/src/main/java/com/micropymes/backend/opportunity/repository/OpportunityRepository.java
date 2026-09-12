package com.micropymes.backend.opportunity.repository;

import com.micropymes.backend.opportunity.domain.Opportunity;
import com.micropymes.backend.opportunity.domain.OpportunityStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import java.util.Optional;
import java.util.UUID;

public interface OpportunityRepository
    extends JpaRepository<Opportunity, UUID> {

  Optional<Opportunity> findByIdAndOrganization_Id(
      UUID opportunityId,
      UUID organizationId);

  boolean existsByCustomer_IdAndOrganization_IdAndArchivedAtIsNullAndStatusIn(
      UUID customerId,
      UUID organizationId,
      Iterable<OpportunityStatus> statuses);

  @Query("""
      SELECT o
      FROM Opportunity o
      WHERE o.organization.id = :organizationId
        AND (
              (:archived = false AND o.archivedAt IS NULL)
              OR
              (:archived = true AND o.archivedAt IS NOT NULL)
            )
        AND (:status IS NULL OR o.status = :status)
        AND (:customerId IS NULL OR o.customer.id = :customerId)
      """)
  Page<Opportunity> search(
      @Param("organizationId") UUID organizationId,
      @Param("status") OpportunityStatus status,
      @Param("customerId") UUID customerId,
      @Param("archived") boolean archived,
      Pageable pageable);

  List<Opportunity> findByOrganization_IdAndArchivedAtIsNullAndStatusIn(
      UUID organizationId,
      Iterable<OpportunityStatus> statuses);
}