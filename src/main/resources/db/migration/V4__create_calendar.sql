CREATE TABLE calendar (
    service_id  VARCHAR(100) PRIMARY KEY,
    monday      BOOLEAN NOT NULL,
    tuesday     BOOLEAN NOT NULL,
    wednesday   BOOLEAN NOT NULL,
    thursday    BOOLEAN NOT NULL,
    friday      BOOLEAN NOT NULL,
    saturday    BOOLEAN NOT NULL,
    sunday      BOOLEAN NOT NULL,
    start_date  DATE    NOT NULL,
    end_date    DATE    NOT NULL,
    CONSTRAINT ck_calendar_dates CHECK ( end_date >= start_date );
);

CREATE TABLE calendar_dates (
    service_id      VARCHAR(100)    NOT NULL,
    date            DATE            NOT NULL,
    exception_type  SMALLINT        NOT NULL CHECK (exception_type IN (1, 2)),
    PRIMARY KEY (service_id, date)
);