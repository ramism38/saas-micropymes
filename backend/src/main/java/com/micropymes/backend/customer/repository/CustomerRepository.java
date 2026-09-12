package com.micropymes.backend.customer.repository;

import com.micropymes.backend.customer.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository
        extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByIdAndOrganization_Id(
            UUID customerId,
            UUID organizationId);

    @Query("""
            SELECT c
            FROM Customer c
            WHERE c.organization.id = :organizationId
              AND (
                    (:archived = false AND c.archivedAt IS NULL)
                    OR
                    (:archived = true AND c.archivedAt IS NOT NULL)
                  )
              AND (
                    :q IS NULL
                    OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(COALESCE(c.companyName, ''))
                        LIKE LOWER(CONCAT('%', :q, '%'))
                    OR LOWER(COALESCE(c.email, ''))
                        LIKE LOWER(CONCAT('%', :q, '%'))
                  )
            """)
    Page<Customer> search(
            @Param("organizationId") UUID organizationId,
            @Param("q") String q,
            @Param("archived") boolean archived,
            Pageable pageable);
}