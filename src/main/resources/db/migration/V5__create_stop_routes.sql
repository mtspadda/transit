CREATE TABLE stop_routes(
    stop_id VARCHAR(100) NOT NULL REFERENCES stops (stop_id),
    route_id VARCHAR(100) NOT NULL REFERENCES routes (route_id),
    PRIMARY KEY  (stop_id, route_id)
);