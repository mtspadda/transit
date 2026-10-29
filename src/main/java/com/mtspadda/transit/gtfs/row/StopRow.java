package com.mtspadda.transit.gtfs.row;

import org.apache.commons.csv.CSVRecord;

import static com.mtspadda.transit.gtfs.row.CsvFields.optional;

public record StopRow (String stopId, String stopCode, String stopName, Double stopLat, Double stopLon) {

    public static StopRow from(CSVRecord r){
        return new StopRow(
                r.get("stop_id"),
                optional(r,"stop_code"),
                r.get("stop_name"),
                Double.parseDouble(r.get("stop_lat")),
                Double.parseDouble(r.get("stop_lon"))
        );
    }
}
