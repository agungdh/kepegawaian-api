CREATE TABLE departments (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid        UUID NOT NULL DEFAULT gen_random_uuid(),

    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(150) NOT NULL,
    description VARCHAR(500),

    created_at TIMESTAMPTZ,
    created_by BIGINT,
    updated_at TIMESTAMPTZ,
    updated_by BIGINT,
    deleted_at TIMESTAMPTZ,
    deleted_by BIGINT
);

CREATE INDEX idx_departments_uuid
    ON departments USING hash (uuid);

CREATE UNIQUE INDEX uq_departments_code_active
    ON departments (code)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_departments_deleted_at
    ON departments (deleted_at);
