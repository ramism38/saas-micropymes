package com.micropymes.backend.organization.repository;

import com.micropymes.backend.organization.domain.OrganizationMember;
import com.micropymes.backend.organization.domain.OrganizationRole;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationMemberRepository
                extends JpaRepository<OrganizationMember, UUID> {

        Optional<OrganizationMember> findByOrganizationIdAndUserId(
                        UUID organizationId,
                        UUID userId);

        List<OrganizationMember> findByOrganizationIdAndActiveTrue(
                        UUID organizationId);

        boolean existsByOrganizationIdAndUserIdAndActiveTrue(
                        UUID organizationId,
                        UUID userId);

        List<OrganizationMember> findByUser_IdAndActiveTrue(
                        UUID userId);

        Optional<OrganizationMember> findByOrganization_IdAndUser_IdAndActiveTrue(
                        UUID organizationId,
                        UUID userId);

        Optional<OrganizationMember> findByIdAndOrganization_Id(
                        UUID memberId,
                        UUID organizationId);

        Optional<OrganizationMember> findByOrganization_IdAndUser_Id(
                        UUID organizationId,
                        UUID userId);

        long countByOrganization_IdAndRoleAndActiveTrue(
                        UUID organizationId,
                        OrganizationRole role);
}