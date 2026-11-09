package com.mtspadda.transit.arrival;

record Prediction(
        ArrivalStatus status,
        Integer delaySeconds
) {

    static final Prediction NONE = new Prediction(ArrivalStatus.SCHEDULE, null);
}
