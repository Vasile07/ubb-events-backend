CREATE TABLE IF NOT EXISTS public.membership_request_history (
    history_id BIGSERIAL PRIMARY KEY,
    membership_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    requested_at TIMESTAMP WITH TIME ZONE NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_membership_request_history_user
        FOREIGN KEY (user_id) REFERENCES public.users (user_id),
    CONSTRAINT fk_membership_request_history_organization
        FOREIGN KEY (organization_id) REFERENCES public.organizations (organization_id),
    CONSTRAINT ck_membership_request_history_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_membership_request_history_membership
    ON public.membership_request_history (membership_id);
CREATE INDEX IF NOT EXISTS ix_membership_request_history_organization_status
    ON public.membership_request_history (organization_id, status);
