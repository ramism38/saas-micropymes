package com.micropymes.backend.organization.repository;

import com.micropymes.backend.organization.domain.Organization;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository
        extends JpaRepository<Organization, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select o
            from Organization o
            where o.id = :organizationId
            """)
    Optional<Organization> findByIdForUpdate(
            @Param("organizationId")
            UUID organizationId
    );
}