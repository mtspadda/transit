package com.mtspadda.transit.gtfs.row;

import org.apache.commons.csv.CSVRecord;

import static com.mtspadda.transit.gtfs.row.CsvFields.*;

public record TripRow(String tripId, String routeId, String serviceId,
                      String tripHeadsign, Integer directionId) {

    public static TripRow from(CSVRecord r) {
        return new TripRow(
                r.get("trip_id"),
                r.get("route_id"),
                r.get("service_id"),
                optional(r, "trip_headsign"),
                optionalInt(r, "direction_id")
        );
    }
}
