package com.micropymes.backend.opportunity.domain;

import com.micropymes.backend.customer.domain.Customer;
import com.micropymes.backend.organization.domain.Organization;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "opportunities",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_opportunity_tenant",
            columnNames = {"organization_id", "id"}
        )
    }
)
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OpportunityStatus status = OpportunityStatus.NEW;

    @Column(
        name = "estimated_value",
        precision = 12,
        scale = 2
    )
    private BigDecimal estimatedValue;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "lost_reason", length = 255)
    private String lostReason;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "archived_at")
    private Instant archivedAt;

    protected Opportunity() {
    }

    public Opportunity(
            Organization organization,
            Customer customer,
            String title
    ) {
        this.organization = organization;
        this.customer = customer;
        this.title = title;
        this.status = OpportunityStatus.NEW;
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

    public void archive() {
        this.archivedAt = Instant.now();
    }

    public void restore() {
        this.archivedAt = null;
    }

    public boolean isArchived() {
        return archivedAt != null;
    }

    public boolean isClosed() {
        return status == OpportunityStatus.WON
                || status == OpportunityStatus.LOST;
    }

    public UUID getId() {
        return id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public OpportunityStatus getStatus() {
        return status;
    }

    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    public String getCurrency() {
        return currency;
    }

    public String getLostReason() {
        return lostReason;
    }

    public Instant getClosedAt() {
        return closedAt;
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

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEstimatedValue(BigDecimal estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void changeStatus(
        OpportunityStatus newStatus,
        String lostReason,
        Instant now
) {
    this.status = newStatus;

    if (newStatus == OpportunityStatus.WON) {
        this.closedAt = now;
        this.lostReason = null;
        return;
    }

    if (newStatus == OpportunityStatus.LOST) {
        this.closedAt = now;
        this.lostReason = lostReason;
        return;
    }

    this.closedAt = null;
    this.lostReason = null;
}
}