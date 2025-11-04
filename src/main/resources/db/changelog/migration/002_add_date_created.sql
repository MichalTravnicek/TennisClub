--liquibase formatted sql

--changeset mtravnicek:3

ALTER TABLE court ADD created timestamp(6);
ALTER TABLE customer ADD created timestamp(6);
ALTER TABLE surface ADD created timestamp(6);
ALTER TABLE reservation ADD created timestamp(6);
