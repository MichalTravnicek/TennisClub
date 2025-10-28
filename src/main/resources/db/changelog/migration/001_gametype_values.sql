--liquibase formatted sql

--changeset mtravnicek:2

INSERT INTO GAME_TYPE (ID, NAME, PRICE_MULTIPLIER) VALUES (1, 'Singles', 1.0);
INSERT INTO GAME_TYPE (ID, NAME, PRICE_MULTIPLIER) VALUES (2, 'Doubles', 1.5);

--rollback DELETE FROM GAME_TYPE WHERE ID = 1
--rollback DELETE FROM GAME_TYPE WHERE ID = 2
