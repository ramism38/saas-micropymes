package com.micropymes.backend.integration;

import com.micropymes.backend.organization.domain.Organization;
import com.micropymes.backend.organization.domain.OrganizationMember;
import com.micropymes.backend.organization.domain.OrganizationRole;
import com.micropymes.backend.organization.repository.OrganizationMemberRepository;
import com.micropymes.backend.organization.repository.OrganizationRepository;
import com.micropymes.backend.user.domain.User;
import com.micropymes.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PersistenceIntegrationTest
        extends AbstractPostgresIntegrationTest {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationMemberRepository memberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayMigrationsAreApplied() {

        Integer migrations =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE success = true
                        """,
                        Integer.class
                );

        assertThat(migrations)
                .isNotNull()
                .isGreaterThanOrEqualTo(2);
    }

    @Test
    @Transactional
    void canPersistOrganizationUserAndOwnerMembership() {

        Organization organization =
                new Organization(
                        "Empresa Test",
                        "EUR"
                );

        organizationRepository.saveAndFlush(
                organization
        );

        User user =
                new User(
                        "integration@test.com",
                        "fake-hash",
                        "Integration",
                        "Test"
                );

        userRepository.saveAndFlush(user);

        OrganizationMember member =
                new OrganizationMember(
                        organization,
                        user,
                        OrganizationRole.OWNER
                );

        memberRepository.saveAndFlush(member);

        assertThat(organization.getId())
                .isNotNull();

        assertThat(user.getId())
                .isNotNull();

        assertThat(member.getId())
                .isNotNull();

        assertThat(member.getRole())
                .isEqualTo(
                        OrganizationRole.OWNER
                );

        assertThat(
                memberRepository
                        .findByOrganization_IdAndUser_IdAndActiveTrue(
                                organization.getId(),
                                user.getId()
                        )
        ).isPresent();
    }
}