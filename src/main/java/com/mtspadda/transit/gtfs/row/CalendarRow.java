package com.mtspadda.transit.gtfs.row;

import org.apache.commons.csv.CSVRecord;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.mtspadda.transit.gtfs.row.CsvFields.optional;

public record CalendarRow(
        String serviceId,
        boolean monday,
        boolean tuesday,
        boolean wednesday,
        boolean thursday,
        boolean friday,
        boolean saturday,
        boolean sunday,
        LocalDate startDate,
        LocalDate endDate
) {
    public static CalendarRow from(CSVRecord r){
        return new CalendarRow(
                r.get("service_id"),
                day(r, "monday"),
                day(r, "tuesday"),
                day(r, "wednesday"),
                day(r, "thursday"),
                day(r, "friday"),
                day(r, "saturday"),
                day(r, "sunday"),
                date(r, "start_date"),
                date(r, "end_date")
        );
    }

    private static boolean day(CSVRecord r, String column) {
        return "1".equals(r.get(column));
    }

    private static LocalDate date(CSVRecord r, String column) {
        return LocalDate.parse(r.get(column), DateTimeFormatter.BASIC_ISO_DATE);
    }
}


