-- MediSlot core schema.  This migration only creates new application tables;
-- it does not modify or remove any pre-existing Supabase tables.
create table if not exists app_users (
    id uuid primary key,
    email varchar(254) not null unique,
    password_hash varchar(100) not null,
    display_name varchar(120) not null,
    role varchar(20) not null check (role in ('PATIENT', 'DOCTOR', 'HOSPITAL', 'ADMIN')),
    enabled boolean not null default true,
    email_verified boolean not null default false,
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp
);

create table if not exists patient_profiles (
    id uuid primary key,
    user_id uuid not null unique references app_users(id),
    phone varchar(32),
    date_of_birth date,
    gender varchar(32),
    emergency_contact varchar(160),
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp
);

create table if not exists doctor_profiles (
    id uuid primary key,
    user_id uuid not null unique references app_users(id),
    registration_number varchar(100) not null unique,
    qualification varchar(500),
    biography text,
    years_of_experience integer not null default 0 check (years_of_experience between 0 and 80),
    consultation_fee numeric(12,2) not null default 0 check (consultation_fee >= 0),
    city varchar(120),
    address varchar(500),
    profile_image_url varchar(2048),
    status varchar(20) not null default 'PENDING' check (status in ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED')),
    rejection_reason varchar(500),
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp
);

create table if not exists hospital_profiles (
    id uuid primary key,
    user_id uuid not null unique references app_users(id),
    facility_name varchar(180) not null,
    registration_number varchar(100) not null unique,
    phone varchar(32),
    address varchar(500),
    city varchar(120),
    description text,
    status varchar(20) not null default 'PENDING' check (status in ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED')),
    rejection_reason varchar(500),
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp
);

create table if not exists specializations (
    id uuid primary key,
    name varchar(120) not null unique,
    slug varchar(140) not null unique,
    active boolean not null default true,
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp
);

create table if not exists doctor_specializations (
    doctor_id uuid not null references doctor_profiles(id) on delete cascade,
    specialization_id uuid not null references specializations(id),
    primary key (doctor_id, specialization_id)
);

create table if not exists hospital_departments (
    id uuid primary key,
    hospital_id uuid not null references hospital_profiles(id) on delete cascade,
    name varchar(160) not null,
    description varchar(1000),
    unique (hospital_id, name)
);

create table if not exists hospital_doctors (
    hospital_id uuid not null references hospital_profiles(id) on delete cascade,
    doctor_id uuid not null references doctor_profiles(id) on delete cascade,
    primary key (hospital_id, doctor_id)
);

create table if not exists provider_documents (
    id uuid primary key,
    doctor_id uuid references doctor_profiles(id) on delete cascade,
    hospital_id uuid references hospital_profiles(id) on delete cascade,
    document_type varchar(60) not null,
    storage_path varchar(1024) not null,
    original_file_name varchar(255) not null,
    content_type varchar(120) not null,
    reviewed_at timestamptz,
    reviewed_by uuid references app_users(id),
    created_at timestamptz not null default current_timestamp,
    check ((doctor_id is not null and hospital_id is null) or (doctor_id is null and hospital_id is not null))
);

create table if not exists appointment_slots (
    id uuid primary key,
    doctor_id uuid not null references doctor_profiles(id),
    starts_at timestamptz not null,
    ends_at timestamptz not null,
    status varchar(20) not null default 'AVAILABLE' check (status in ('AVAILABLE', 'BOOKED', 'BLOCKED')),
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp,
    version bigint not null default 0,
    check (ends_at > starts_at),
    unique (doctor_id, starts_at)
);

create table if not exists appointments (
    id uuid primary key,
    slot_id uuid not null references appointment_slots(id),
    patient_id uuid not null references patient_profiles(id),
    doctor_id uuid not null references doctor_profiles(id),
    status varchar(20) not null default 'BOOKED' check (status in ('BOOKED', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'REJECTED')),
    reason varchar(1000),
    cancellation_reason varchar(500),
    created_at timestamptz not null default current_timestamp,
    updated_at timestamptz not null default current_timestamp
);

-- A historic cancelled appointment does not consume a released slot; only active bookings do.
create unique index if not exists uq_active_appointment_per_slot
    on appointments(slot_id) where status in ('BOOKED', 'CONFIRMED');

create table if not exists notifications (
    id uuid primary key,
    user_id uuid not null references app_users(id) on delete cascade,
    type varchar(60) not null,
    title varchar(180) not null,
    body varchar(1000) not null,
    read_at timestamptz,
    created_at timestamptz not null default current_timestamp
);

create table if not exists audit_logs (
    id uuid primary key,
    actor_user_id uuid references app_users(id),
    action varchar(120) not null,
    entity_type varchar(80) not null,
    entity_id uuid,
    detail varchar(1000),
    created_at timestamptz not null default current_timestamp
);

create table if not exists revoked_tokens (
    id uuid primary key,
    token_id varchar(64) not null unique,
    expires_at timestamptz not null,
    created_at timestamptz not null default current_timestamp
);

create table if not exists password_reset_tokens (
    id uuid primary key,
    user_id uuid not null references app_users(id) on delete cascade,
    token_hash varchar(128) not null unique,
    expires_at timestamptz not null,
    used_at timestamptz,
    created_at timestamptz not null default current_timestamp
);

create index if not exists idx_doctor_profiles_discovery on doctor_profiles(status, city);
create index if not exists idx_hospital_profiles_discovery on hospital_profiles(status, city);
create index if not exists idx_slots_doctor_starts on appointment_slots(doctor_id, starts_at);
create index if not exists idx_appointments_patient_created on appointments(patient_id, created_at desc);
create index if not exists idx_appointments_doctor_created on appointments(doctor_id, created_at desc);
create index if not exists idx_notifications_user_created on notifications(user_id, created_at desc);
create index if not exists idx_audit_logs_created on audit_logs(created_at desc);
