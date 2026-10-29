package com.mtspadda.transit.gtfs.row;

import org.apache.commons.csv.CSVRecord;

public class CsvFields {

    private CsvFields(){

    }

    static String optional(CSVRecord r, String column) {
        if (!r.isSet(column)) {
            return null;
        }
        String value = r.get(column);
        return value.isBlank() ? null : value;
    }

    static Integer optionalInt(CSVRecord r, String column) {
        String value = optional(r, column);
        return value == null ? null : Integer.parseInt(value);
    }
}
