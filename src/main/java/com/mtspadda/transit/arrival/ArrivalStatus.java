package com.mtspadda.transit.arrival;

public enum ArrivalStatus {
    SCHEDULE, // without live info;
    REALTIME, // with live prediction
    CANCELED, // canceled trip
    SKIPPED // bus will skip the stop
}
