CREATE TABLE users (
    id                      BIGSERIAL     PRIMARY KEY,
    name                    VARCHAR(150)  NOT NULL,
    email                   VARCHAR(150)  NOT NULL,
    password                VARCHAR(255)  NOT NULL,
    role                    VARCHAR(20)   NOT NULL,
    enabled                 BOOLEAN       NOT NULL DEFAULT TRUE,
    reset_token_hash        VARCHAR(64),
    reset_token_expires_at  TIMESTAMP,
    created_at              TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT uq_users_email UNIQUE (email)
);
