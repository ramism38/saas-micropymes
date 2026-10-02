package com.micropymes.backend.organization.service;

import com.micropymes.backend.common.error.ApiException;
import com.micropymes.backend.common.error.ErrorCode;
import com.micropymes.backend.followup.domain.FollowUp;
import com.micropymes.backend.followup.domain.FollowUpStatus;
import com.micropymes.backend.followup.repository.FollowUpRepository;
import com.micropymes.backend.organization.domain.OrganizationMember;
import com.micropymes.backend.organization.domain.OrganizationRole;
import com.micropymes.backend.organization.dto.AddMemberRequest;
import com.micropymes.backend.organization.dto.MemberResponse;
import com.micropymes.backend.organization.dto.UpdateMemberRoleRequest;
import com.micropymes.backend.organization.repository.OrganizationMemberRepository;
import com.micropymes.backend.organization.repository.OrganizationRepository;
import com.micropymes.backend.user.domain.User;
import com.micropymes.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class OrganizationMemberService {

    private final OrganizationMemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final FollowUpRepository followUpRepository;
    private final OrganizationAccessService accessService;

    public OrganizationMemberService(
            OrganizationMemberRepository memberRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            FollowUpRepository followUpRepository,
            OrganizationAccessService accessService
    ) {
        this.memberRepository = memberRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.followUpRepository = followUpRepository;
        this.accessService = accessService;
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAll(
            UUID organizationId,
            UUID currentUserId
    ) {
        accessService.requireMember(
                organizationId,
                currentUserId
        );

        return memberRepository
                .findByOrganizationIdAndActiveTrue(
                        organizationId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MemberResponse addMember(
            UUID organizationId,
            UUID currentUserId,
            AddMemberRequest request
    ) {

        /*
         * Bloqueamos la organización antes de comprobar
         * los permisos y modificar sus miembros.
         *
         * De esta forma, dos operaciones administrativas
         * sobre la misma organización no pueden ejecutarse
         * simultáneamente tomando decisiones con información
         * desactualizada.
         */
        lockOrganization(organizationId);

        OrganizationMember currentMember =
                accessService.requireOwner(
                        organizationId,
                        currentUserId
                );

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ApiException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        if (!user.isEnabled()) {
            throw new ApiException(
                    ErrorCode.ACCOUNT_DISABLED
            );
        }

        OrganizationMember existing =
                memberRepository
                        .findByOrganization_IdAndUser_Id(
                                organizationId,
                                user.getId()
                        )
                        .orElse(null);

        if (existing != null) {

            if (existing.isActive()) {
                throw new ApiException(
                        ErrorCode.MEMBER_ALREADY_EXISTS
                );
            }

            existing.reactivate(
                    request.role()
            );

            return toResponse(existing);
        }

        OrganizationMember member =
                new OrganizationMember(
                        currentMember.getOrganization(),
                        user,
                        request.role()
                );

        memberRepository.save(member);

        return toResponse(member);
    }

    @Transactional
    public MemberResponse changeRole(
            UUID organizationId,
            UUID memberId,
            UUID currentUserId,
            UpdateMemberRoleRequest request
    ) {

        /*
         * Todas las modificaciones administrativas
         * de miembros de una misma organización pasan
         * primero por este bloqueo.
         */
        lockOrganization(organizationId);

        accessService.requireOwner(
                organizationId,
                currentUserId
        );

        OrganizationMember member =
                findMember(
                        organizationId,
                        memberId
                );

        if (!member.isActive()) {
            throw new ApiException(
                    ErrorCode.MEMBER_INACTIVE
            );
        }

        /*
         * Si estamos degradando a un OWNER,
         * debemos comprobar que no sea el último.
         *
         * Como la organización está bloqueada,
         * otra transacción no puede hacer la misma
         * comprobación simultáneamente sobre esta
         * organización.
         */
        if (member.getRole()
                == OrganizationRole.OWNER
                && request.role()
                != OrganizationRole.OWNER) {

            ensureNotLastOwner(
                    organizationId
            );
        }

        member.setRole(
                request.role()
        );

        return toResponse(member);
    }

    @Transactional
    public void deactivate(
            UUID organizationId,
            UUID memberId,
            UUID currentUserId
    ) {

        /*
         * También bloqueamos antes de desactivar.
         *
         * Esto evita que dos OWNER distintos puedan
         * ser desactivados simultáneamente dejando
         * la organización sin propietarios.
         */
        lockOrganization(organizationId);

        accessService.requireOwner(
                organizationId,
                currentUserId
        );

        OrganizationMember member =
                findMember(
                        organizationId,
                        memberId
                );

        if (!member.isActive()) {
            throw new ApiException(
                    ErrorCode.MEMBER_INACTIVE
            );
        }

        if (member.getRole()
                == OrganizationRole.OWNER) {

            ensureNotLastOwner(
                    organizationId
            );
        }

        /*
         * Si el miembro tenía follow-ups pendientes
         * asignados, quedan sin asignación.
         */
        List<FollowUp> pendingFollowUps =
                followUpRepository
                        .findByOrganization_IdAndAssignedToMember_IdAndStatus(
                                organizationId,
                                memberId,
                                FollowUpStatus.PENDING
                        );

        pendingFollowUps.forEach(
                followUp ->
                        followUp.setAssignedToMember(
                                null
                        )
        );

        member.deactivate();
    }

    /*
     * Obtiene y bloquea la fila de Organization
     * mediante PESSIMISTIC_WRITE.
     *
     * El bloqueo se mantiene hasta que finaliza
     * la transacción del método que lo ha llamado.
     */
    private void lockOrganization(
            UUID organizationId
    ) {
        organizationRepository
                .findByIdForUpdate(
                        organizationId
                )
                .orElseThrow(() ->
                        new ApiException(
                                ErrorCode.ORGANIZATION_NOT_FOUND
                        )
                );
    }

    private OrganizationMember findMember(
            UUID organizationId,
            UUID memberId
    ) {
        return memberRepository
                .findByIdAndOrganization_Id(
                        memberId,
                        organizationId
                )
                .orElseThrow(() ->
                        new ApiException(
                                ErrorCode.MEMBER_NOT_FOUND
                        )
                );
    }

    private void ensureNotLastOwner(
            UUID organizationId
    ) {
        long activeOwners =
                memberRepository
                        .countByOrganization_IdAndRoleAndActiveTrue(
                                organizationId,
                                OrganizationRole.OWNER
                        );

        if (activeOwners <= 1) {
            throw new ApiException(
                    ErrorCode.LAST_OWNER_REQUIRED
            );
        }
    }

    private MemberResponse toResponse(
            OrganizationMember member
    ) {
        User user =
                member.getUser();

        return new MemberResponse(
                member.getId(),
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                member.getRole(),
                member.isActive(),
                member.getJoinedAt(),
                member.getLeftAt()
        );
    }
}