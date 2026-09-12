package com.micropymes.backend.auth.service;

import com.micropymes.backend.auth.dto.CurrentUserResponse;
import com.micropymes.backend.auth.dto.LoginRequest;
import com.micropymes.backend.auth.dto.OrganizationMembershipResponse;
import com.micropymes.backend.auth.dto.RegisterRequest;
import com.micropymes.backend.common.error.ApiException;
import com.micropymes.backend.common.error.ErrorCode;
import com.micropymes.backend.organization.domain.Organization;
import com.micropymes.backend.organization.domain.OrganizationMember;
import com.micropymes.backend.organization.domain.OrganizationRole;
import com.micropymes.backend.organization.repository.OrganizationMemberRepository;
import com.micropymes.backend.organization.repository.OrganizationRepository;
import com.micropymes.backend.user.domain.User;
import com.micropymes.backend.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.micropymes.backend.auth.dto.LoginRequest;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

        private final UserRepository userRepository;
        private final OrganizationRepository organizationRepository;
        private final OrganizationMemberRepository memberRepository;
        private final PasswordEncoder passwordEncoder;

        public AuthService(
                        UserRepository userRepository,
                        OrganizationRepository organizationRepository,
                        OrganizationMemberRepository memberRepository,
                        PasswordEncoder passwordEncoder) {
                this.userRepository = userRepository;
                this.organizationRepository = organizationRepository;
                this.memberRepository = memberRepository;
                this.passwordEncoder = passwordEncoder;
        }

        @Transactional
        public CurrentUserResponse register(RegisterRequest request) {

                String email = request.email()
                                .trim()
                                .toLowerCase(Locale.ROOT);

                if (userRepository.existsByEmailIgnoreCase(email)) {
                        throw new ApiException(
                                        ErrorCode.EMAIL_ALREADY_REGISTERED);
                }

                User user = new User(
                                email,
                                passwordEncoder.encode(request.password()),
                                request.firstName().trim(),
                                request.lastName().trim());

                Organization organization = new Organization(
                                request.organizationName().trim(),
                                "EUR");

                userRepository.save(user);
                organizationRepository.save(organization);

                OrganizationMember member = new OrganizationMember(
                                organization,
                                user,
                                OrganizationRole.OWNER);

                memberRepository.save(member);

                return toCurrentUserResponse(
                                user,
                                List.of(member));
        }

        @Transactional(readOnly = true)
        public CurrentUserResponse getCurrentUser(UUID userId) {

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ApiException(
                                                ErrorCode.USER_NOT_FOUND));

                List<OrganizationMember> memberships = memberRepository
                                .findByUser_IdAndActiveTrue(userId);

                return toCurrentUserResponse(
                                user,
                                memberships);
        }

        private CurrentUserResponse toCurrentUserResponse(
                        User user,
                        List<OrganizationMember> memberships) {

                List<OrganizationMembershipResponse> organizations = memberships.stream()
                                .map(member -> new OrganizationMembershipResponse(
                                                member.getOrganization().getId(),
                                                member.getOrganization().getName(),
                                                member.getRole()))
                                .toList();

                return new CurrentUserResponse(
                                user.getId(),
                                user.getEmail(),
                                user.getFirstName(),
                                user.getLastName(),
                                organizations);
        }

        @Transactional(readOnly = true)
        public CurrentUserResponse login(LoginRequest request) {

                String email = request.email()
                                .trim()
                                .toLowerCase(Locale.ROOT);

                User user = userRepository
                                .findByEmailIgnoreCase(email)
                                .orElseThrow(() -> new ApiException(
                                                ErrorCode.INVALID_CREDENTIALS));

                if (!user.isEnabled()) {
                        throw new ApiException(
                                        ErrorCode.ACCOUNT_DISABLED);
                }

                if (!passwordEncoder.matches(
                                request.password(),
                                user.getPasswordHash())) {
                        throw new ApiException(
                                        ErrorCode.INVALID_CREDENTIALS);
                }

                List<OrganizationMember> memberships = memberRepository
                                .findByUser_IdAndActiveTrue(user.getId());

                return toCurrentUserResponse(
                                user,
                                memberships);
        }
}