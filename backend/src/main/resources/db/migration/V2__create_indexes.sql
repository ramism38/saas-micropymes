CREATE INDEX idx_org_members_user
ON organization_members(user_id);


CREATE INDEX idx_customers_org_active
ON customers(organization_id)
WHERE archived_at IS NULL;


CREATE INDEX idx_opportunities_customer
ON opportunities(customer_id);


CREATE INDEX idx_opportunities_org_active
ON opportunities(organization_id, status)
WHERE archived_at IS NULL;


CREATE INDEX idx_quotes_opportunity
ON quotes(opportunity_id);


CREATE INDEX idx_quotes_org_status
ON quotes(organization_id, status);


CREATE UNIQUE INDEX uq_quote_one_accepted_per_opportunity
ON quotes(opportunity_id)
WHERE status = 'ACCEPTED';


CREATE INDEX idx_followups_opportunity
ON follow_ups(opportunity_id);


CREATE INDEX idx_followups_pending
ON follow_ups(organization_id, scheduled_at)
WHERE status = 'PENDING';


CREATE INDEX idx_followups_assigned_member
ON follow_ups(assigned_to_member_id)
WHERE assigned_to_member_id IS NOT NULL;


CREATE INDEX idx_activities_opportunity_date
ON activities(opportunity_id, occurred_at DESC);