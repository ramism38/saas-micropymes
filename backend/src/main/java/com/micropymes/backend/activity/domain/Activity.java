package com.micropymes.backend.activity.domain;

import com.micropymes.backend.opportunity.domain.Opportunity;
import com.micropymes.backend.organization.domain.Organization;
import com.micropymes.backend.organization.domain.OrganizationMember;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_member_id")
    private OrganizationMember actorMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private ActivityType type;

    @Column(name = "description")
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Activity() {
    }

    public Activity(
            Organization organization,
            Opportunity opportunity,
            OrganizationMember actorMember,
            ActivityType type,
            String description
    ) {
        this.organization = organization;
        this.opportunity = opportunity;
        this.actorMember = actorMember;
        this.type = type;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (this.occurredAt == null) {
            this.occurredAt = now;
        }

        this.createdAt = now;
    }

    public UUID getId() {
        return id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public Opportunity getOpportunity() {
        return opportunity;
    }

    public OrganizationMember getActorMember() {
        return actorMember;
    }

    public ActivityType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}