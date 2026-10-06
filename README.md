# Multi-Tenant Expense Approval Platform

> A production-style expense approval system with a double-entry ledger,
> row-level tenant isolation, and an append-only audit trail.

**Live demo:** https://frontend-production-7fb2.up.railway.app  
**Demo video:** [90-second walkthrough](https://youtube.com/watch?v=YOUR_ID)

## What it does

- Employees submit expenses with categories and descriptions
- Managers approve or reject them
- Approvals create double-entry journal entries (DEBIT expense, CREDIT cash)
- Approvals can be reversed — the ledger is append-only, reversals create
  new entries with opposite signs
- Tenant data is isolated at both app (TenantContext) and DB (RLS) levels
- Complete audit trail logs every action

## Architecture

## Architecture

```
┌─────────────┐
│   React     │ (http://localhost:5173)
│  Frontend   │
└──────┬──────┘
       │ JWT token
       │
┌──────▼──────────────────┐
│   Spring Boot API       │
│ (http://localhost:8080) │
│                         │
│ - JWT validation        │
│ - TenantContext         │
│ - Business logic        │
└──────┬──────────────────┘
       │ Postgres connection
       │
┌──────▼──────────────────┐
│  PostgreSQL (5432)      │
│                         │
│ - RLS policies          │
│ - Journal ledger        │
│ - Audit logs            │
└─────────────────────────┘
```


## Key design decisions

### Append-only ledger with reversals
When an expense is approved, a journal entry is created with two lines:
- DEBIT Expense:Travel +$50 (positive = money to travel account)
- CREDIT Company:Cash -$50 (negative = money from cash)

If that expense was a mistake, we create a REVERSAL entry:
- DEBIT Company:Cash +$50 (undo the credit)
- CREDIT Expense:Travel -$50 (undo the debit)

The original entry stays for audit. The ledger tracks every change. No deletes.

Database enforces: `SUM(amount_minor) per entry = 0` ✓

### Tenant isolation at two layers
1. **Application:** `TenantContext` extracts tenant from JWT, carries it through request
2. **Database:** PostgreSQL RLS policies + `SET LOCAL app.tenant_id`

If application code forgets `WHERE tenant_id = ?`, the database still won't 
leak other tenants' data. Defense-in-depth.

### Signed amounts instead of direction field
Instead of:
```sql
amount: 50, direction: 'DEBIT'
amount: 50, direction: 'CREDIT'
```

We use:
```sql
amount: +50   (debit)
amount: -50   (credit)
```

Simpler query: `SUM(amount) = 0` instead of comparing debit totals to credit totals.

### SERIALIZABLE transactions
When two managers approve simultaneously, could they corrupt the ledger? No.
Every approval uses `@Transactional(isolation = SERIALIZABLE)`. If conflicts 
occur, Spring retries. The ledger is guaranteed consistent.

## What I learned

1. **Financial systems think differently** — Correctness beats performance
2. **Multi-tenancy is a security mindset** — Don't just filter, enforce at DB
3. **Testing invariants beats coverage** — Tests that prove ledger balances = gold
4. **Append-only means auditability** — Never delete, reverse instead

## What I'd do differently (production)

- Redis caching for expensive balance queries
- Async job queue for anomaly detection
- Short-lived JWTs + refresh tokens
- Rate limiting per tenant
- Multi-currency support (store FX rate at approval)
- Encrypted storage for sensitive fields

## Tech stack

| Component | Technology | Why |
|-----------|-----------|-----|
| Backend | Spring Boot 3, Jakarta | Enterprise standard, excellent transaction handling |
| Frontend | React 18, Vite | Fast dev experience, type-safe with TS |
| Database | PostgreSQL 16 | ACID, RLS, JSON, reliability |
| Auth | JWT (HS256) | Stateless, scales horizontally |
| Deployment | Railway | Simple, automated builds, free tier sufficient for demo |

## Testing

```bash
mvn test
```

Runs critical invariant tests:

**Data Isolation (RLS)**
- Create two tenants
- Insert expenses for each
- Set RLS context for tenant A
- Query with no WHERE clause
- Assert only tenant A's expenses returned ✓

**Ledger Correctness**
- Try to insert unbalanced lines → DB rejects ✓
- Insert balanced lines → succeeds ✓
- SUM(amount_minor) = 0 ✓

## Local development

```bash
# Start everything
docker compose up

# Backend:  http://localhost:8080
# Frontend: http://localhost:5173
# Database: localhost:5432

# Register a test tenant
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "tenantName": "acme-corp",
    "email": "admin@acme.com",
    "password": "password",
    "fullName": "Alice Admin"
  }'
```

## Endpoints

| Method | Endpoint | What it does |
|--------|----------|-------------|
| POST | `/auth/register` | Create tenant + admin user |
| POST | `/auth/login` | Get JWT token |
| POST | `/api/expenses` | Submit expense (any user) |
| GET | `/api/expenses` | List (filters by user/tenant) |
| PATCH | `/api/expenses/:id/approve` | Approve + create ledger (manager only) |
| PATCH | `/api/expenses/:id/reject` | Reject (manager only) |
| POST | `/api/expenses/:id/reverse` | Reverse an approval (admin only) |
| GET | `/api/accounts` | List accounts with balances |
| GET | `/api/audit-logs` | List all actions (admin only) |

## Code quality

- JUnit tests for critical invariants (RLS, ledger balance)
- Structured logging with tenant context
- Global exception handling with proper HTTP status codes
- DTOs for request/response (never expose entities)
- Repository pattern for data access

## Repository

[GitHub Link](https://github.com/k-jovik/multi-tenant-expense-platform)
