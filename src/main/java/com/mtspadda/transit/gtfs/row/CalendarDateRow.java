package com.mtspadda.transit.gtfs.row;

import org.apache.commons.csv.CSVRecord;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record CalendarDateRow(
        String serviceId,
        LocalDate date,
        int exceptionType
) {

    public static CalendarDateRow from(CSVRecord r){
        return new CalendarDateRow(
                r.get("service_id"),
                LocalDate.parse(r.get("date"), DateTimeFormatter.BASIC_ISO_DATE),
                Integer.parseInt(r.get("exception_type"))
        );
    }
}
