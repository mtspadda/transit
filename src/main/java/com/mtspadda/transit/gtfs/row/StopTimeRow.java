package com.mtspadda.transit.gtfs.row;

import com.mtspadda.transit.gtfs.GtfsTime;
import org.apache.commons.csv.CSVRecord;

import static com.mtspadda.transit.gtfs.GtfsTime.toSeconds;
import static com.mtspadda.transit.gtfs.row.CsvFields.optional;
import static com.mtspadda.transit.gtfs.row.CsvFields.optionalInt;

public record StopTimeRow(
        String tripId,
        String stopId,
        Integer stopSequence,
        Integer arrivalTime,
        Integer departureTime,
        String stopHeadsign,
        Integer pickupType,
        Integer dropOffType
) {

    static StopTimeRow from(CSVRecord r) {
        return new StopTimeRow(
                r.get("trip_id"),
                r.get("stop_id"),
                Integer.parseInt(r.get("stop_sequence")),
                toSeconds(optional(r, "arrival_time")),
                toSeconds(optional(r, "departure_time")),
                optional(r, "stop_headsign"),
                optionalInt(r, "pickup_type"),
                optionalInt(r, "drop_off_type")
        );
    }
}