CREATE TABLE accounts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_number VARCHAR(34) NOT NULL UNIQUE,
    account_name VARCHAR(120) NOT NULL,
    currency CHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_accounts_currency_format
        CHECK (currency ~ '^[A-Z]{3}$'),

    CONSTRAINT chk_accounts_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);