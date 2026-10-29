package com.mtspadda.transit.gtfs.row;

import org.apache.commons.csv.CSVRecord;

import static com.mtspadda.transit.gtfs.row.CsvFields.optional;

public record StopRow (String stopId, String stopCode, String stopName, String stopLat, String stopLon) {

    static StopRow from(CSVRecord r){
        return new StopRow(
                r.get("stop_id"),
                optional(r,"stop_code"),
                r.get("stop_name"),
                r.get("stop_lat"),
                r.get("stop_lon")
        );
    }
}
