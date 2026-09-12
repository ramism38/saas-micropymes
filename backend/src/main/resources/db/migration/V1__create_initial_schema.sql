CREATE TABLE organizations (
    id UUID PRIMARY KEY,

    name VARCHAR(150) NOT NULL,
    default_currency CHAR(3) NOT NULL DEFAULT 'EUR',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);


CREATE TABLE users (
    id UUID PRIMARY KEY,

    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(150) NOT NULL,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);


CREATE TABLE organization_members (
    id UUID PRIMARY KEY,

    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,

    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    joined_at TIMESTAMPTZ NOT NULL,
    left_at TIMESTAMPTZ,

    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_organization_members_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id),

    CONSTRAINT fk_organization_members_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT uq_organization_member
        UNIQUE (organization_id, user_id),

    CONSTRAINT uq_organization_member_tenant
        UNIQUE (organization_id, id),

    CONSTRAINT chk_organization_member_role
        CHECK (role IN ('OWNER', 'MEMBER'))
);


CREATE TABLE customers (
    id UUID PRIMARY KEY,

    organization_id UUID NOT NULL,

    name VARCHAR(150) NOT NULL,
    company_name VARCHAR(150),

    email VARCHAR(255),
    phone VARCHAR(50),

    notes TEXT,

    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    archived_at TIMESTAMPTZ,

    CONSTRAINT fk_customers_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id),

    CONSTRAINT uq_customer_tenant
        UNIQUE (organization_id, id)
);


CREATE TABLE opportunities (
    id UUID PRIMARY KEY,

    organization_id UUID NOT NULL,
    customer_id UUID NOT NULL,

    title VARCHAR(200) NOT NULL,
    description TEXT,

    status VARCHAR(30) NOT NULL,

    estimated_value NUMERIC(12,2),
    currency CHAR(3),

    lost_reason VARCHAR(255),
    closed_at TIMESTAMPTZ,

    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    archived_at TIMESTAMPTZ,

    CONSTRAINT fk_opportunities_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id),

    CONSTRAINT fk_opportunities_customer_tenant
        FOREIGN KEY (organization_id, customer_id)
        REFERENCES customers(organization_id, id),

    CONSTRAINT uq_opportunity_tenant
        UNIQUE (organization_id, id),

    CONSTRAINT chk_opportunity_status
        CHECK (
            status IN (
                'NEW',
                'CONTACTED',
                'PROPOSAL_SENT',
                'NEGOTIATION',
                'WON',
                'LOST'
            )
        ),

    CONSTRAINT chk_opportunity_estimated_value
        CHECK (
            estimated_value IS NULL
            OR estimated_value >= 0
        ),

    CONSTRAINT chk_opportunity_currency
        CHECK (
            (estimated_value IS NULL AND currency IS NULL)
            OR
            (estimated_value IS NOT NULL AND currency IS NOT NULL)
        ),

    CONSTRAINT chk_opportunity_closed_at
        CHECK (
            (status IN ('WON', 'LOST') AND closed_at IS NOT NULL)
            OR
            (status NOT IN ('WON', 'LOST') AND closed_at IS NULL)
        ),

    CONSTRAINT chk_opportunity_lost_reason
        CHECK (
            status = 'LOST'
            OR lost_reason IS NULL
        )
);


CREATE TABLE quotes (
    id UUID PRIMARY KEY,

    organization_id UUID NOT NULL,
    opportunity_id UUID NOT NULL,

    amount NUMERIC(12,2) NOT NULL,
    currency CHAR(3) NOT NULL,

    status VARCHAR(20) NOT NULL,

    sent_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ,

    notes TEXT,

    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_quotes_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id),

    CONSTRAINT fk_quotes_opportunity_tenant
        FOREIGN KEY (organization_id, opportunity_id)
        REFERENCES opportunities(organization_id, id),

    CONSTRAINT chk_quote_amount
        CHECK (amount > 0),

    CONSTRAINT chk_quote_status
        CHECK (
            status IN (
                'DRAFT',
                'SENT',
                'ACCEPTED',
                'REJECTED',
                'EXPIRED'
            )
        ),

    CONSTRAINT chk_quote_sent_at
        CHECK (
            (status = 'DRAFT' AND sent_at IS NULL)
            OR
            (status <> 'DRAFT' AND sent_at IS NOT NULL)
        ),

    CONSTRAINT chk_quote_expiration
        CHECK (
            expires_at IS NULL
            OR sent_at IS NULL
            OR expires_at >= sent_at
        )
);


CREATE TABLE follow_ups (
    id UUID PRIMARY KEY,

    organization_id UUID NOT NULL,
    opportunity_id UUID NOT NULL,
    assigned_to_member_id UUID,

    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,

    scheduled_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,

    notes TEXT,

    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_followups_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id),

    CONSTRAINT fk_followups_opportunity_tenant
        FOREIGN KEY (organization_id, opportunity_id)
        REFERENCES opportunities(organization_id, id),

    CONSTRAINT fk_followups_assigned_member_tenant
        FOREIGN KEY (organization_id, assigned_to_member_id)
        REFERENCES organization_members(organization_id, id),

    CONSTRAINT chk_followup_type
        CHECK (
            type IN (
                'CALL',
                'EMAIL',
                'WHATSAPP',
                'MEETING',
                'OTHER'
            )
        ),

    CONSTRAINT chk_followup_status
        CHECK (
            status IN (
                'PENDING',
                'COMPLETED',
                'CANCELLED'
            )
        ),

    CONSTRAINT chk_followup_completed_at
        CHECK (
            (status = 'COMPLETED' AND completed_at IS NOT NULL)
            OR
            (status <> 'COMPLETED' AND completed_at IS NULL)
        )
);


CREATE TABLE activities (
    id UUID PRIMARY KEY,

    organization_id UUID NOT NULL,
    opportunity_id UUID NOT NULL,
    actor_member_id UUID,

    type VARCHAR(30) NOT NULL,
    description TEXT,

    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_activities_organization
        FOREIGN KEY (organization_id)
        REFERENCES organizations(id),

    CONSTRAINT fk_activities_opportunity_tenant
        FOREIGN KEY (organization_id, opportunity_id)
        REFERENCES opportunities(organization_id, id),

    CONSTRAINT fk_activities_actor_member_tenant
        FOREIGN KEY (organization_id, actor_member_id)
        REFERENCES organization_members(organization_id, id),

    CONSTRAINT chk_activity_type
        CHECK (
            type IN (
                'OPPORTUNITY_CREATED',
                'NOTE',
                'CALL',
                'EMAIL',
                'MEETING',
                'QUOTE_SENT',
                'QUOTE_EXPIRED',
                'STATUS_CHANGE',
                'FOLLOW_UP_COMPLETED',
                'SYSTEM',
                'OTHER'
            )
        )
);