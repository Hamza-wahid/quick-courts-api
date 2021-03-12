DROP DATABASE IF EXISTS wcc_tennis;
CREATE DATABASE wcc_tennis;
\c wcc_tennis;

DROP TABLE IF EXISTS "user_auth";

CREATE TABLE IF NOT EXISTS "users" (
    "id" SERIAL PRIMARY KEY,
    "email" VARCHAR(50) NOT NULL UNIQUE,
    "password" VARCHAR(255) NOT NULL,
    "first_name" VARCHAR(100) NOT NULL,
    "last_name" VARCHAR(100) NOT NULL,
    "gender" INT NOT NULL,
    "membership_type" INT NOT NULL
);