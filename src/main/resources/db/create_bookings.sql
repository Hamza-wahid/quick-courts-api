DROP TABLE IF EXISTS "bookings";


CREATE TABLE bookings (
  "id" SERIAL PRIMARY KEY,
  "user_id" BIGINT NOT NULL,
  "court_number" INT NOT NULL,
  "date" VARCHAR(10) NOT NULL,
  "start_time" VARCHAR(5) NOT NULL,
  "end_time" VARCHAR(5) NOT NULL
);

INSERT INTO bookings (user_id, court_number, date, start_time, end_time) VALUES (1, 2, '2021-01-12', '12:00','13:00');