package com.micropymes.backend.organization.domain;

import com.micropymes.backend.user.domain.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "organization_members",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_organization_member",
            columnNames = {"organization_id", "user_id"}
        )
    }
)
public class OrganizationMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private OrganizationRole role;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected OrganizationMember() {
    }

    public OrganizationMember(
            Organization organization,
            User user,
            OrganizationRole role
    ) {
        this.organization = organization;
        this.user = user;
        this.role = role;
        this.active = true;
    }

    @PrePersist
    protected void onCreate() {
        this.joinedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.leftAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public User getUser() {
        return user;
    }

    public OrganizationRole getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public Instant getLeftAt() {
        return leftAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setRole(OrganizationRole role) {
        this.role = role;
    }

    public void reactivate(OrganizationRole role) {
    this.active = true;
    this.leftAt = null;
    this.role = role;
}
}