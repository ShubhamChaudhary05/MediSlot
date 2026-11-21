-- Incremented after a password reset so existing JWT access tokens become invalid.
alter table app_users add column if not exists auth_version integer not null default 0 check (auth_version >= 0);
