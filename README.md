# MediSlot

MediSlot is a local-first healthcare discovery and appointment-booking application. It uses a Next.js frontend, a Spring Boot REST API, and PostgreSQL (Supabase is supported) with Flyway migrations.

## What is implemented

- Patient, doctor, and hospital account registration with BCrypt password hashes.
- JWT access tokens, short configurable expiry, token revocation on logout, and server-side role checks.
- No public administrator registration. An optional environment-gated bootstrap account is available for the first administrator.
- Additive, versioned PostgreSQL schema migration (`V1__core_schema.sql`).
- Approved-provider-only public doctor and hospital search.
- Doctor profile editing, protected slot creation, public availability lookup, transactional patient booking, cancellation, and doctor confirmation/rejection/completion transitions.
- Pessimistic slot locking plus database constraints to prevent concurrent double booking.
- Provider approval/rejection/suspension endpoints and a protected administration metrics endpoint.
- API-backed patient/provider authentication screens, doctor and facility search, doctor detail/booking, and role-aware dashboards.

## Requirements (Windows)

- Node.js 22 LTS and npm
- JDK 21 (the project was verified with a newer compatible JDK as well)
- Maven 3.9+
- A PostgreSQL database; Supabase PostgreSQL is supported

## Configuration

Never commit `backend/.env` or `frontend/.env.local`. The repository contains safe examples only.

1. Copy `backend/.env.example` to `backend/.env`.
2. Supply the Supabase connection values. `DB_URL` must begin with `jdbc:postgresql://` and use `sslmode=require` for Supabase.
3. Create a JWT key, then set it in `JWT_SECRET`:

   ```powershell
   [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
   ```

4. Optionally set `ADMIN_BOOTSTRAP_EMAIL`, `ADMIN_BOOTSTRAP_PASSWORD` (at least 12 characters), and `ADMIN_BOOTSTRAP_NAME` for the one-time initial admin. Remove the bootstrap password after that account has been created.
5. Copy `frontend/.env.local.example` to `frontend/.env.local` if it does not already exist. The default API URL is `http://localhost:8080/api`.

The backend automatically imports `backend/.env` locally. Operating-system environment variables take precedence.

## Run locally

Start the API:

```powershell
cd backend
mvn spring-boot:run
```

Flyway validates and applies migrations during startup. It only creates MediSlot's own tables; Hibernate is configured to validate, never alter, the schema.

Start the frontend in a second terminal:

```powershell
cd frontend
npm run dev
```

Open `http://localhost:3000`.

## Verify

```powershell
# backend
cd backend
mvn test

# frontend
cd frontend
npm run lint
npm run build
```

## API overview

All normal API responses use `{ data, timestamp }`; error responses do not expose stack traces or database details.

| Group | Key routes |
| --- | --- |
| Health | `GET /api/health`; `GET /api/db-check` is administrator-only |
| Auth | `POST /api/auth/register`, `/login`, `/logout`, `/password-reset/request`, `/password-reset/confirm` |
| Discovery | `GET /api/doctors`, `/api/doctors/{id}`, `/api/doctors/{id}/slots`, `GET /api/hospitals` |
| Patient booking | `POST/GET /api/appointments`, `POST /api/appointments/{id}/cancel` |
| Doctor workspace | `GET/PUT /api/doctors/me`, `POST /api/doctors/me/slots`, appointment management routes |
| Admin | `GET /api/admin/dashboard`, user listing, and provider status routes (administrator only) |

Use an `Authorization: Bearer <access-token>` header for protected endpoints.

## Operational notes

- The Supabase database was inspected before migration; it had no MediSlot application tables. Flyway schema version 1 was then applied successfully.
- Password-reset requests store only a SHA-256 token digest. No email provider is configured, so the service deliberately does **not** claim to send email or log reset tokens. Add a vetted mail provider adapter before enabling password resets for real users.
- File uploads, private provider-document storage, email delivery/reminders, reviews, and a complete administrator UI remain future work. Do not treat this repository as medical-records software or production healthcare infrastructure without legal, privacy, security, accessibility, observability, and penetration-testing review.
- Profile images and medical documents should be integrated with authorized Supabase Storage buckets; never place verification documents in public storage.

## Security model

Passwords are hashed with BCrypt. Roles from browser payloads are not trusted for authorization; protected routes are checked in Spring Security and at service ownership boundaries. Provider registrations begin `PENDING` and public discovery only returns `APPROVED` records. Bookings use a database transaction, a row lock, slot state, and a partial unique PostgreSQL index to resist double booking.
