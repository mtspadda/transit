package com.mtspadda.transit.arrival;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Not a @Query inside a JpaRepository because a JpaRepository need to be bounded
 * to a entity, this query puts together three tables and return a result.
 */
@Repository
@RequiredArgsConstructor
class ArrivalRepository {

    private static final String SCHEDULED_ARRIVALS = """
            WITH active_services AS (
                SELECT service_id
                FROM calendar
                WHERE CAST(:serviceDate AS date) BETWEEN start_date AND end_date
                  AND CASE EXTRACT(ISODOW FROM CAST(:serviceDate AS date))
                          WHEN 1 THEN monday
                          WHEN 2 THEN tuesday
                          WHEN 3 THEN wednesday
                          WHEN 4 THEN thursday
                          WHEN 5 THEN friday
                          WHEN 6 THEN saturday
                          WHEN 7 THEN sunday
                      END
                UNION
                SELECT service_id FROM calendar_dates
                WHERE date = CAST(:serviceDate AS date) AND exception_type = 1
                EXCEPT
                SELECT service_id FROM calendar_dates
                WHERE date = CAST(:serviceDate AS date) AND exception_type = 2
            )
            SELECT st.trip_id,
                   st.arrival_time,
                   COALESCE(r.route_short_name, r.route_long_name) AS route_name,
                   t.trip_headsign
            FROM stop_times st
            JOIN trips t  ON t.trip_id = st.trip_id
            JOIN routes r ON r.route_id = t.route_id
            WHERE st.stop_id = :stopId
              AND st.arrival_time BETWEEN :fromSeconds AND :toSeconds
              AND t.service_id IN (SELECT service_id FROM active_services)
            ORDER BY st.arrival_time
            """;

    private final NamedParameterJdbcTemplate jdbc;

    List<ScheduledStopTime> findScheduled(String stopId, LocalDate serviceDate, int fromSeconds, int toSeconds) {
        var params = new MapSqlParameterSource()
                .addValue("stopId", stopId)
                .addValue("serviceDate", serviceDate)
                .addValue("fromSeconds", fromSeconds)
                .addValue("toSeconds", toSeconds);

        return jdbc.query(SCHEDULED_ARRIVALS, params, (rs, rowNum) -> new ScheduledStopTime(
                rs.getString("trip_id"),
                rs.getInt("arrival_time"),
                rs.getString("route_name"),
                rs.getString("trip_headsign")));
    }


}
