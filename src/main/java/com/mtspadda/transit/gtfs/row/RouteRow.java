package com.mtspadda.transit.gtfs.row;

import org.apache.commons.csv.CSVRecord;

import static com.mtspadda.transit.gtfs.row.CsvFields.optional;

public record RouteRow(String routeId, String agencyId, String routeShortName, String routeLongName, Integer routeType) {

    static RouteRow from(CSVRecord r){
        return new RouteRow(
                r.get("route_id"),
                optional(r,"agency_id"),
                optional(r,"route_short_name"),
                optional(r,"route_long_name"),
                Integer.parseInt(r.get("route_type"))
        );
    }

    /**
     *     @Id
     *     private String routeId;
     *
     *     private String agencyId;
     *     private String routeShortName;
     *     private String routeLongName;
     *     private Integer routeType;
     */
}
