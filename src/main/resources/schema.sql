CREATE TABLE oauth2_authorized_client
(
    client_registration_id  varchar(100)                            NOT NULL,
    principal_name          varchar(200) UNIQUE                     NOT NULL,
    access_token_type       varchar(100)                            NOT NULL,
    access_token_value      blob                                    NOT NULL,
    access_token_issued_at  timestamp                               NOT NULL,
    access_token_expires_at timestamp                               NOT NULL,
    access_token_scopes     varchar(1000) DEFAULT NULL,
    refresh_token_value     blob          DEFAULT NULL,
    refresh_token_issued_at timestamp     DEFAULT NULL,
    created_at              timestamp     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (client_registration_id, principal_name)
);

CREATE TABLE check_in
(
    id            BIGSERIAL PRIMARY KEY,
    uuid          VARCHAR(36) unique NOT NULL,
    owner         VARCHAR(200)       NOT NULL,
    name          VARCHAR(255)       NOT NULL,
    created_at    TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    intro_message TEXT,
    outro_message TEXT,
    CONSTRAINT PRINCIPAL_NAME_FK FOREIGN KEY (owner)
        REFERENCES oauth2_authorized_client (principal_name),
    UNIQUE (owner, name)
);

CREATE INDEX idx_check_in_owner ON check_in (owner);
CREATE INDEX idx_check_in_uuid ON check_in (uuid);

CREATE TABLE CHECK_IN_QUESTION
(
    id            BIGSERIAL PRIMARY KEY,
    uuid          VARCHAR(36) unique NOT NULL,
    check_in_id   BIGSERIAL,
    question      TEXT,
    order_number  INTEGER,
    is_active     BOOLEAN  default true,
    CONSTRAINT CHECK_IN_ID_FK FOREIGN KEY (check_in_id)
        REFERENCES check_in (id),
    UNIQUE (check_in_id, order_number)
);

-- Add comments to table and columns
COMMENT
ON TABLE check_in IS 'Stores check-in sessions with intro/outro messages';
COMMENT
ON COLUMN check_in.owner IS 'Principal name from oauth2_authorized_client';
COMMENT
ON COLUMN check_in.name IS 'Name of the check-in session (unique per owner)';
COMMENT
ON COLUMN check_in.uuid IS 'UUID of the check-in session';
COMMENT
ON COLUMN check_in.intro_message IS 'Optional introduction message for check-in';
COMMENT
ON COLUMN check_in.outro_message IS 'Optional conclusion message for check-in';

COMMENT
ON TABLE CHECK_IN_QUESTION IS 'Stores check-in questions';
COMMENT
ON COLUMN CHECK_IN_QUESTION.uuid IS 'UUID of the check-in question';
COMMENT
ON COLUMN CHECK_IN_QUESTION.question IS 'Text representing the question';
COMMENT
ON COLUMN CHECK_IN_QUESTION.order_number IS 'Order number starting from 0';
COMMENT
ON COLUMN CHECK_IN_QUESTION.is_active IS 'Status flag';
