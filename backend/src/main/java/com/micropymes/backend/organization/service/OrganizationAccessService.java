package com.micropymes.backend.organization.service;

import com.micropymes.backend.common.error.ApiException;
import com.micropymes.backend.common.error.ErrorCode;
import com.micropymes.backend.organization.domain.OrganizationMember;
import com.micropymes.backend.organization.domain.OrganizationRole;
import com.micropymes.backend.organization.repository.OrganizationMemberRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrganizationAccessService {

    private final OrganizationMemberRepository memberRepository;

    public OrganizationAccessService(
            OrganizationMemberRepository memberRepository
    ) {
        this.memberRepository = memberRepository;
    }

    public OrganizationMember requireMember(
            UUID organizationId,
            UUID userId
    ) {
        return memberRepository
                .findByOrganization_IdAndUser_IdAndActiveTrue(
                        organizationId,
                        userId
                )
                .orElseThrow(() ->
                        new ApiException(
                                ErrorCode.ORGANIZATION_NOT_FOUND
                        )
                );
    }

    public OrganizationMember requireOwner(
            UUID organizationId,
            UUID userId
    ) {
        OrganizationMember member =
                requireMember(organizationId, userId);

        if (member.getRole() != OrganizationRole.OWNER) {
            throw new ApiException(
                    ErrorCode.OWNER_PERMISSION_REQUIRED
            );
        }

        return member;
    }
}