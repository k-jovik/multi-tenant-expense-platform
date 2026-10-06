-- V7: create a least-privilege role for the application to connect as.
--
-- Postgres superusers bypass RLS unconditionally, so if the app connects as
-- the default "postgres" superuser, RLS is effectively disabled. This role
-- is a non-superuser with only the permissions the app needs, so RLS
-- policies are actually enforced.
--
-- The role has no password here — each environment (local, Railway) sets
-- its own via ALTER ROLE.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'app_user') THEN
CREATE ROLE app_user LOGIN;
END IF;
END
$$;

-- Schema access
GRANT USAGE ON SCHEMA public TO app_user;

-- Data access on existing tables
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;

-- Data access on any tables/sequences created by future migrations
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO app_user;

-- Prevent the app role from being treated as a superuser or bypassing RLS
ALTER ROLE app_user NOSUPERUSER NOBYPASSRLS NOCREATEDB NOCREATEROLE;