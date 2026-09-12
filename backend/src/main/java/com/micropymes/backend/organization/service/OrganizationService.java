package com.micropymes.backend.organization.service;

import com.micropymes.backend.organization.domain.Organization;
import com.micropymes.backend.organization.domain.OrganizationMember;
import com.micropymes.backend.organization.dto.OrganizationResponse;
import com.micropymes.backend.organization.dto.UpdateOrganizationRequest;
import com.micropymes.backend.organization.repository.OrganizationMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class OrganizationService {

    private final OrganizationMemberRepository memberRepository;
    private final OrganizationAccessService accessService;

    public OrganizationService(
            OrganizationMemberRepository memberRepository,
            OrganizationAccessService accessService
    ) {
        this.memberRepository = memberRepository;
        this.accessService = accessService;
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> findOrganizationsForUser(
            UUID userId
    ) {
        return memberRepository
                .findByUser_IdAndActiveTrue(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrganizationResponse findById(
            UUID organizationId,
            UUID userId
    ) {
        OrganizationMember membership =
                accessService.requireMember(
                        organizationId,
                        userId
                );

        return toResponse(membership);
    }

    @Transactional
    public OrganizationResponse update(
            UUID organizationId,
            UUID userId,
            UpdateOrganizationRequest request
    ) {
        OrganizationMember membership =
                accessService.requireOwner(
                        organizationId,
                        userId
                );

        Organization organization =
                membership.getOrganization();

        if (request.name() != null) {
            organization.setName(
                    request.name().trim()
            );
        }

        if (request.defaultCurrency() != null) {
            organization.setDefaultCurrency(
                    request.defaultCurrency()
                            .toUpperCase(Locale.ROOT)
            );
        }

        return toResponse(membership);
    }

    private OrganizationResponse toResponse(
            OrganizationMember membership
    ) {
        Organization organization =
                membership.getOrganization();

        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getDefaultCurrency(),
                organization.isEnabled(),
                membership.getRole()
        );
    }
}