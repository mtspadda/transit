CREATE TABLE trips (
                       trip_id        VARCHAR(100) PRIMARY KEY,
                       route_id       VARCHAR(100) NOT NULL REFERENCES routes (route_id),
                       service_id     VARCHAR(100) NOT NULL,
                       trip_headsign  VARCHAR(255),
                       direction_id   SMALLINT     CHECK (direction_id IN (0, 1))
);

CREATE INDEX idx_trips_route_id ON trips (route_id);

CREATE TABLE stop_times (
                            id              BIGSERIAL    PRIMARY KEY,

                            trip_id         VARCHAR(100) NOT NULL REFERENCES trips (trip_id),
                            stop_id         VARCHAR(100) NOT NULL REFERENCES stops (stop_id),
                            stop_sequence   INTEGER      NOT NULL,

    -- Segundos desde a meia-noite do dia de serviço.
    -- "24:20:00" vira 87600, então nunca dá problema com viagens que viram a noite.
                            arrival_time    INTEGER,
                            departure_time  INTEGER,

                            stop_headsign   VARCHAR(255),   -- às vezes o letreiro muda no meio da viagem
                            pickup_type     SMALLINT,       -- 0 = embarque normal, 1 = sem embarque...
                            drop_off_type   SMALLINT,

                            CONSTRAINT uq_stop_times_trip_seq UNIQUE (trip_id, stop_sequence),
                            CONSTRAINT ck_stop_times_sequence CHECK (stop_sequence >= 0),
                            CONSTRAINT ck_stop_times_order    CHECK (departure_time IS NULL
                                OR arrival_time IS NULL
                                OR departure_time >= arrival_time)
);

-- "O que passa nesta parada?" filtra por stop_id e ordena por horário
CREATE INDEX idx_stop_times_stop_arrival ON stop_times (stop_id, arrival_time);