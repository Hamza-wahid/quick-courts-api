DROP TABLE IF EXISTS "bookings";


CREATE TABLE bookings (
  "id" SERIAL PRIMARY KEY,
  "user_id" BIGINT NOT NULL,
  "court_number" INT NOT NULL,
  "year" INT NOT NULL,
  "month" INT NOT NULL,
  "day" INT NOT NULL,
  "start_time" INT NOT NULL,
  "end_time" INT NOT NULL
);

INSERT INTO bookings (user_id, court_number, year, month, day, start_time, end_time) VALUES (1, 2, 2021, 3, 7, 12,13);