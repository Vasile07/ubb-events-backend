-- T68: nullable organization category used by the organization workspace header.
-- Existing organizations intentionally remain NULL until populated by the team.
ALTER TABLE public.organizations
    ADD COLUMN IF NOT EXISTS category VARCHAR(255);
