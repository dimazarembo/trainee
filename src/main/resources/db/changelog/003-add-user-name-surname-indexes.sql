--liquibase formatted sql

--changeset dzarembo:003
CREATE INDEX lower_case_name ON users (LOWER(name));
CREATE INDEX lower_case_surname ON users (LOWER(surname));

--rollback DROP INDEX IF EXISTS lower_case_surname;
--rollback DROP INDEX IF EXISTS lower_case_name;
