package com.micropymes.backend.activity.repository;

import com.micropymes.backend.activity.domain.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface ActivityRepository
                extends JpaRepository<Activity, UUID> {

        List<Activity> findByOpportunity_IdAndOrganization_IdOrderByOccurredAtDesc(
                        UUID opportunityId,
                        UUID organizationId);

        Optional<Activity> findFirstByOpportunity_IdAndOrganization_IdOrderByOccurredAtDesc(
                        UUID opportunityId,
                        UUID organizationId);
}