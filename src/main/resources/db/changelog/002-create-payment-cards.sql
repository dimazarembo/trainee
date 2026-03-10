--liquibase formatted sql

--changeset dzarembo:payment-cards-001
CREATE TABLE payment_cards(
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_payment_cards_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    number VARCHAR(100) UNIQUE NOT NULL,
    holder VARCHAR(100) NOT NULL ,
    expiration_date DATE NOT NULL ,
    active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
--rollback DROP TABLE IF EXISTS payment_cards;

--changeset dzarembo:payment-cards-002
CREATE INDEX idx_payments_cards_users_id ON payment_cards(user_id);
--rollback DROP INDEX IF EXISTS idx_payments_cards_users_id;

