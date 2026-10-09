# Multi-Tenant Expense Approval Platform

A production-style expense approval system with a double-entry ledger and
row-level tenant isolation enforced at the database layer.

**Live demo:** https://frontend-production-7fb2.up.railway.app

## What it does

- Employees submit expenses with a category, amount, and description
- Managers approve or reject submitted expenses
- Approval writes a double-entry journal entry (one debit, one credit)
- Approved expenses can be reversed — the ledger is append-only, so a
  reversal appends a new entry with negated lines instead of deleting or
  editing the original
- Tenant data is isolated at both the application layer (`TenantContext`)
  and the database layer (Postgres Row-Level Security)

## Architecture

```
┌──────────────────────┐
│ React + Vite (5173) │
└──────────┬───────────┘
│ JWT in Authorization header
▼
┌──────────────────────┐
│ Spring Boot API │
│ - JwtFilter │
│ - TenantContext │
│ - RlsContext │
│ - Business logic │
└──────────┬───────────┘
│ app_user (non-superuser, subject to RLS)
▼
┌──────────────────────┐
│ PostgreSQL 16 │
│ - RLS policies │
│ - Journal ledger │
│ - Balance trigger │
└──────────────────────┘
```


## Key design decisions

### Append-only ledger with reversals

When an expense is approved, a journal entry is created with two lines:

DEBIT Expense:<category> +amount_minor
CREDIT Payable:<userId> -amount_minor

To reverse an approved expense, a **new** journal entry is appended. Its
lines mirror the original with **negated amounts on the same accounts**:

DEBIT Expense:<category> -amount_minor
CREDIT Payable:<userId> +amount_minor


The original entry is never modified or deleted. Balances are computed as
`SUM(amount_minor)` per account, so the reversal nets the effect to zero
while the audit trail preserves both events.

A Postgres constraint trigger on `journal_lines` rejects any commit where
an entry's lines don't sum to zero. Unbalanced entries are impossible at
the database level, regardless of application code.

### Tenant isolation at two layers

**Application.** `TenantContext` (a `ThreadLocal`) carries the tenant id
through the request; every repository query filters by `tenantId`.

**Database.** RLS policies on all tenant-scoped tables filter by
`current_setting('app.tenant_id')`, set per transaction via `SET LOCAL`.
Policies are `FORCED` so the table owner is also subject to them. The
application connects as `app_user`, a non-superuser role with only the
privileges it needs — because Postgres superusers bypass RLS
unconditionally, running the app as the default `postgres` role would
silently disable this layer.

If application code ever omits a `WHERE tenant_id = ?` clause, the
database still refuses to return other tenants' rows.

### Signed amounts instead of a direction column

Each journal line stores a signed `amount_minor`. Positive is a debit,
negative is a credit. Balance queries become `SUM(amount_minor)`; there's
no separate `direction` column to keep in sync.

### SERIALIZABLE for state transitions

`approve`, `reject`, and `reverse` run at `Isolation.SERIALIZABLE`. If
two managers approve the same expense concurrently, one transaction
commits and the other fails with a serialization error, which rolls it
back — no double journal entry is possible.

Retry-on-serialization-failure is not implemented. A production system
would add retry (e.g. `@Retryable`) so the loser sees a 409 instead of a
transient 500.

### Frontend architecture

**Auth via React Context.** `AuthProvider` (in `contexts/AuthContext.tsx`)
holds the current user and exposes `login` / `logout` through a `useAuth()`
hook. On mount it hydrates state from `localStorage`, verifying that both
`token` and `user` are present before trusting the cached user object.

**Protected routes.** `App.tsx` wraps the authenticated area in a
`<ProtectedRoute>` component that reads `useAuth()` and redirects to
`/login` if no user is present. Routes under that wrapper share a common
`<Layout>` (navbar + outlet).

**Axios instance with interceptors.** A single `api` instance in
`lib/api.ts` centralizes:

- *Request interceptor:* attaches `Authorization: Bearer <token>` from
  `localStorage` to every outbound request.
- *Response interceptor:* on 401, clears stored credentials and redirects
  to `/login` — so an expired token logs the user out without any
  per-page code.

**`getApiErrorMessage` helper.** Normalizes the varied error shapes from
Spring (custom `ApiException` bodies with a `message` field, validation
errors, generic 500s) into a single user-facing string. Every mutation's
`onError` handler uses it, so the same rendering path handles a 409
"already processed" and a network failure.

**Server state with TanStack Query.** `useExpenses()` and `useAccounts()`
wrap `useQuery` with stable query keys (`['expenses']`, `['accounts']`).
Mutations call `queryClient.setQueryData(['expenses'], ...)` to update the
list cache in place after an approve/reject/reverse, and invalidate
`['accounts']` so derived balances refetch. This keeps the UI consistent
without full-page reloads.

**Forms.** React Hook Form handles validation and submission on the login,
register, and add-expense pages. Form-level errors (server-side failures)
render below the form, and `isSubmitting` drives the loading state on the
submit button.

## What I learned

- **Debits and credits must balance to zero per journal entry** — and this
  invariant is best enforced by the database. A Postgres constraint
  trigger blocks any commit that would violate it, so the app can't
  corrupt the ledger even by accident.

- **RLS only works when the connecting role isn't a superuser.** Postgres
  superusers bypass RLS unconditionally. The app connects as a
  least-privilege role (`app_user`) while Flyway uses admin credentials
  for migrations.

- **`SET LOCAL` scope matters.** It's transaction-scoped, so it must be
  set inside an active transaction and clears automatically at commit or
  rollback — exactly what you want for per-request tenant context.

- **A reversal is a first-class operation in an append-only ledger.**
  Preserving history is more valuable than the ability to edit it, and
  reversals as new entries give you both correctness and auditability.

## What I'd do differently in production

- Redis caching for expensive account-balance queries
- Async job queue for anomaly detection on expense submission
- Short-lived JWTs plus refresh tokens (access tokens are valid for 24 hours)
- Rate limiting on `/auth/login` and `/auth/register`
- FX conversion at approval time (multi-currency expenses currently post at par)
- Automatic retry on serialization failures

## Tech stack

| Layer      | Technology |
|------------|-----------|
| Backend    | Java 21, Spring Boot 4.1, Spring Security, Spring Data JPA, Flyway |
| Frontend   | React 19, Vite 8, TypeScript 6, TanStack Query, Tailwind CSS 4, shadcn/ui |
| Database   | PostgreSQL 16 (RLS, JSONB, constraint triggers) |
| Auth       | JWT (HS256) |
| Deployment | Railway (frontend, backend, managed Postgres) |

## Testing

```bash
./mvnw test

Two integration tests run against a real Postgres 16 via Testcontainers.
Flyway applies all migrations to a throwaway container, so the tests
exercise the real schema, RLS policies, and trigger — not mocks.

TenantIsolationTest verifies RLS at the database layer:

Seeds two tenants, one expense each

Opens a separate JDBC connection as app_user (non-superuser, subject
to RLS)

Runs SELECT COUNT(*) FROM expenses (no WHERE clause) under three
different app.tenant_id contexts

Asserts 1 (tenant A), 1 (tenant B), 0 (no context)

LedgerBalanceTest verifies the balance trigger:

Attempts to commit a journal entry whose lines sum to +10 → asserts the
commit is rejected and nothing persisted

Commits a balanced entry → asserts both lines are present

Local development
Start Postgres via Docker Compose (no local Postgres installation needed):

docker compose up -d

Then run the backend and frontend in separate terminals:

# Terminal 1: backend on http://localhost:8080
cd backend/expense-platform-api
./mvnw spring-boot:run

# Terminal 2: frontend on http://localhost:5173
cd frontend/expense-platform-web
npm install
npm run dev

Register a test tenant:

curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "tenantName": "acme-corp",
    "email": "admin@acme.com",
    "password": "password123",
    "fullName": "Alice Admin"
  }'
```

## API endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Create a new tenant and admin user |
| POST | `/auth/login` | Authenticate and receive JWT token |
| POST | `/api/expenses` | Submit a new expense |
| GET | `/api/expenses` | List all expenses for the current tenant |
| PATCH | `/api/expenses/{id}/approve` | Approve an expense and create ledger entry (Manager/Admin only) |
| PATCH | `/api/expenses/{id}/reject` | Reject an expense (Manager/Admin only) |
| PATCH | `/api/expenses/{id}/reverse` | Reverse an approved expense (Manager/Admin only; APPROVED status only) |
| GET | `/api/accounts` | List all accounts with calculated balances |



## Known limitations

Single-currency ledger. Expenses may be submitted in any currency;
the ledger is USD and multi-currency expenses post at par without FX
conversion. Production would apply an FX rate at approval time or
maintain per-currency accounts.

No rate limiting. /auth/login and /auth/register are
unthrottled.

Dev JWT secret fallback. application.properties contains a
dev-only default value; production overrides it via the JWT_SECRET
environment variable.

No refresh tokens. Access tokens are valid for 24 hours.

No serialization-failure retry. A rare concurrent-approval race
surfaces as a 500 rather than a friendly 409.

No audit log endpoint yet. The audit_logs table exists in the
schema, but no writes and no read endpoint are implemented.

Deployment
Deployed on Railway: React frontend, Spring Boot backend, and managed
PostgreSQL 16 in the same project. The backend connects as app_user
(non-superuser) so RLS is enforced at runtime; Flyway runs as the admin
role on startup to apply migrations. CORS allowed origins are configured
via the CORS_ALLOWED_ORIGINS environment variable.

Repository
https://github.com/k-jovik/multi-tenant-expense-platform
