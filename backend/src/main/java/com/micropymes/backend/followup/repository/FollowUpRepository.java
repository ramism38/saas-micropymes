package com.micropymes.backend.followup.repository;

import com.micropymes.backend.followup.domain.FollowUp;
import com.micropymes.backend.followup.domain.FollowUpStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowUpRepository
                extends JpaRepository<FollowUp, UUID>,
                JpaSpecificationExecutor<FollowUp> {

        Optional<FollowUp> findByIdAndOrganization_Id(
                        UUID followUpId,
                        UUID organizationId);

        List<FollowUp> findByOpportunity_IdAndOrganization_Id(
                        UUID opportunityId,
                        UUID organizationId);

        List<FollowUp> findByOrganization_IdAndStatus(
                        UUID organizationId,
                        FollowUpStatus status);

        List<FollowUp> findByOrganization_IdAndAssignedToMember_IdAndStatus(
                        UUID organizationId,
                        UUID memberId,
                        FollowUpStatus status);

        List<FollowUp> findByOpportunity_IdAndOrganization_IdAndStatus(
                        UUID opportunityId,
                        UUID organizationId,
                        FollowUpStatus status);

        Optional<FollowUp> findFirstByOpportunity_IdAndOrganization_IdAndStatusOrderByScheduledAtAsc(
                        UUID opportunityId,
                        UUID organizationId,
                        FollowUpStatus status);
}