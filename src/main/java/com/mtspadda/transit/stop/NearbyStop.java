package com.mtspadda.transit.stop;

public interface NearbyStop {
    String getStopId();
    String getStopCode();
    String getStopName();
    double getStopLat();
    double getStopLon();
    long getDistanceMeters();
    String getTransportType();
}
