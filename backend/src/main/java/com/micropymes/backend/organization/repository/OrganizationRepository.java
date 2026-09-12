package com.micropymes.backend.organization.repository;

import com.micropymes.backend.organization.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrganizationRepository
        extends JpaRepository<Organization, UUID> {
}