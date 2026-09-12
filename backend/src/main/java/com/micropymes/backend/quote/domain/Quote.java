package com.micropymes.backend.quote.domain;

import com.micropymes.backend.opportunity.domain.Opportunity;
import com.micropymes.backend.organization.domain.Organization;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quotes")
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @Column(
        name = "amount",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal amount;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QuoteStatus status = QuoteStatus.DRAFT;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "notes")
    private String notes;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Quote() {
    }

    public Quote(
            Organization organization,
            Opportunity opportunity,
            BigDecimal amount,
            String currency
    ) {
        this.organization = organization;
        this.opportunity = opportunity;
        this.amount = amount;
        this.currency = currency;
        this.status = QuoteStatus.DRAFT;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public QuoteStatus getStatus() {
        return status;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
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

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void send(Instant now) {
    this.status = QuoteStatus.SENT;
    this.sentAt = now;
}

public void accept() {
    this.status = QuoteStatus.ACCEPTED;
}

public void reject() {
    this.status = QuoteStatus.REJECTED;
}

public void expire() {
    this.status = QuoteStatus.EXPIRED;
}
}