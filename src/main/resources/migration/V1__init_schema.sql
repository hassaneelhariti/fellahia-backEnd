-- V1__init_schema.sql
-- FellahIA initial database schema

-- ── Extensions ───────────────────────────────────────────────────────────────
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── Enums ─────────────────────────────────────────────────────────────────────
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('FELLAH', 'AVOCAT');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE case_urgency AS ENUM ('NORMAL', 'URGENT', 'VERY_URGENT');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE case_status AS ENUM ('PENDING', 'ACCEPTED', 'IN_PROGRESS', 'CLOSED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- ── Users ─────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name   VARCHAR(150) NOT NULL,
    phone       VARCHAR(20)  NOT NULL UNIQUE,
    email       VARCHAR(150) UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        user_role    NOT NULL,
    verified    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_phone ON users (phone);
CREATE INDEX IF NOT EXISTS idx_users_role  ON users (role);

-- ── Fellah profiles ───────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS fellah_profiles (
    id         UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID           NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    balance    DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    rib        VARCHAR(100)
);

-- ── Lawyer profiles ───────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS lawyer_profiles (
    id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID          NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    bar_number      VARCHAR(50),
    specialization  VARCHAR(200),
    region          VARCHAR(100),
    rating          DECIMAL(2,1)  NOT NULL DEFAULT 0.0,
    total_cases     INTEGER       NOT NULL DEFAULT 0
);

-- ── Legal cases ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS legal_cases (
    id           UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    reference    VARCHAR(30)   NOT NULL UNIQUE,
    fellah_id    UUID          NOT NULL REFERENCES users(id),
    lawyer_id    UUID          REFERENCES users(id),
    title        VARCHAR(300),
    description  TEXT          NOT NULL,
    urgency      case_urgency  NOT NULL,
    status       case_status   NOT NULL DEFAULT 'PENDING',
    cost         DECIMAL(10,2) NOT NULL,
    region       VARCHAR(100),
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_cases_fellah_id ON legal_cases (fellah_id);
CREATE INDEX IF NOT EXISTS idx_cases_lawyer_id ON legal_cases (lawyer_id);
CREATE INDEX IF NOT EXISTS idx_cases_status    ON legal_cases (status);

-- ── Case files ────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS case_files (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id      UUID         NOT NULL REFERENCES legal_cases(id) ON DELETE CASCADE,
    file_name    VARCHAR(255),
    file_type    VARCHAR(50),
    storage_key  VARCHAR(500),
    size_bytes   BIGINT,
    uploaded_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_case_files_case_id ON case_files (case_id);

-- ── Chat messages ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS chat_messages (
    id          UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role        VARCHAR(20) NOT NULL CHECK (role IN ('user', 'assistant')),
    content     TEXT      NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chat_user_id   ON chat_messages (user_id);
CREATE INDEX IF NOT EXISTS idx_chat_created   ON chat_messages (user_id, created_at DESC);

-- ── Token transactions ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS token_transactions (
    id           UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID           NOT NULL REFERENCES users(id),
    case_id      UUID           REFERENCES legal_cases(id),
    amount       DECIMAL(10,2)  NOT NULL,
    type         VARCHAR(20)    NOT NULL CHECK (type IN ('DEBIT', 'CREDIT')),
    description  VARCHAR(300),
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON token_transactions (user_id);

-- ── OTP codes ─────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS otp_codes (
    id          UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code        VARCHAR(6) NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    used        BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_otp_user_id ON otp_codes (user_id);
