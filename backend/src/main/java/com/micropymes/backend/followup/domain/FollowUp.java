package com.micropymes.backend.followup.domain;

import com.micropymes.backend.opportunity.domain.Opportunity;
import com.micropymes.backend.organization.domain.Organization;
import com.micropymes.backend.organization.domain.OrganizationMember;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "follow_ups")
public class FollowUp {

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
    @JoinColumn(name = "assigned_to_member_id")
    private OrganizationMember assignedToMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private FollowUpType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FollowUpStatus status = FollowUpStatus.PENDING;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "notes")
    private String notes;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected FollowUp() {
    }

    public FollowUp(
            Organization organization,
            Opportunity opportunity,
            FollowUpType type,
            Instant scheduledAt) {
        this.organization = organization;
        this.opportunity = opportunity;
        this.type = type;
        this.scheduledAt = scheduledAt;
        this.status = FollowUpStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
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

    public OrganizationMember getAssignedToMember() {
        return assignedToMember;
    }

    public FollowUpType getType() {
        return type;
    }

    public FollowUpStatus getStatus() {
        return status;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getNotes() {
        return notes;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setAssignedToMember(OrganizationMember assignedToMember) {
        this.assignedToMember = assignedToMember;
    }

    public void setType(FollowUpType type) {
        this.type = type;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void cancel() {
        this.status = FollowUpStatus.CANCELLED;
        this.completedAt = null;
    }

    public void complete(Instant now) {
        this.status = FollowUpStatus.COMPLETED;
        this.completedAt = now;
    }
}