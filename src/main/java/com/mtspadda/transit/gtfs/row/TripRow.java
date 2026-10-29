package com.mtspadda.transit.gtfs.row;

import org.apache.commons.csv.CSVRecord;

public record TripRow(String routeId, String serviceId, String tripHeadsign, Integer directionId) {

    static TripRow from(CSVRecord r){
        return new TripRow(
                r.get("route_id"),
                r.get("service_id"),
                r.get("trip_headsign"),
                Integer.parseInt(r.get("direction_id"))
        );
    }
}
