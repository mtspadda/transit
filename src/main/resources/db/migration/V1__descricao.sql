CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE routes (
    route_id    VARCHAR(100) PRIMARY KEY,
    agency_id   VARCHAR(100),
    route_short_name    VARCHAR(50),
    route_long_name     VARCHAR(250),
    route_type      SMALLINT    NOT NULL
);

CREATE TABLE stops (
    stop_id     VARCHAR(100)    PRIMARY KEY,
    stop_code   VARCHAR(50),
    stop_name   VARCHAR(255)    NOT NULL,
    stop_lat    DOUBLE PRECISION NOT NULL,
    stop_lon    DOUBLE PRECISION NOT NULL,
    location   GEOGRAPHY(Point, 4326)
               GENERATED ALWAYS AS (
                   ST_SetSRID(ST_MakePoint(stop_lon, stop_lat), 4326)::geography
               ) STORED
);
)