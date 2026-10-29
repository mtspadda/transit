package com.mtspadda.transit.gtfs;

public final class GtfsTime {
    private GtfsTime() {
    }
    /**
     * @return seconds, or null if value is null
     * @throws IllegalArgumentException if is an invalid format
     */
    public static Integer toSeconds(String time) {
        if (time == null || time.isBlank()) return null;

        String[] parts = time.trim().split(":");
        if (parts.length != 3){
            throw invalid(time, null);
        }

        int hours, minutes, seconds;
        try {
            hours = Integer.parseInt(parts[0]);
            minutes = Integer.parseInt(parts[1]);
            seconds = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            throw invalid(time, e);
        }

        if (hours < 0 || minutes < 0 || minutes > 59 || seconds < 0 || seconds > 59) {
            throw invalid(time, null);
        }

        return hours * 3600 + minutes * 60 + seconds;
    }

    private static IllegalArgumentException invalid(String time, Throwable cause) {
        return new IllegalArgumentException("Invalid GTFS time: '" + time + "'", cause);
    }

}
