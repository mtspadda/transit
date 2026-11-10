package com.mtspadda.transit.stop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StopRepository extends JpaRepository<Stops, String> {
    @Query(value = """
        SELECT s.stop_id   AS "stopId",
               s.stop_code AS "stopCode",
               s.stop_name AS "stopName",
               s.stop_lat  AS "stopLat",
               s.stop_lon  AS "stopLon",
               ST_Distance(s.location,
                           CAST(ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) AS geography)) AS "distanceMeters",
               (SELECT CASE
                           WHEN bool_or(r.route_type = 0
                                        AND COALESCE(r.route_short_name, r.route_long_name) ILIKE '%red%')   THEN 'LUAS_RED'
                           WHEN bool_or(r.route_type = 0
                                        AND COALESCE(r.route_short_name, r.route_long_name) ILIKE '%green%') THEN 'LUAS_GREEN'
                           WHEN bool_or(r.route_type = 0) THEN 'TRAM'
                           WHEN bool_or(r.route_type = 2) THEN 'RAIL'
                           ELSE 'BUS'
                       END
                FROM stop_routes sr
                JOIN routes r ON r.route_id = sr.route_id
                WHERE sr.stop_id = s.stop_id) AS "transportType"
        FROM stops s
        WHERE ST_DWithin(s.location,
                         CAST(ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) AS geography),
                         :radius)
        ORDER BY "distanceMeters"
        LIMIT :limit
        """, nativeQuery = true)
    List<NearbyStop> findNearby(@Param("lat") double lat,
                                @Param("lon") double lon,
                                @Param("radius") double radiusMeters,
                                @Param("limit") int limit);

    List<Stops> findByStopCode(String stopCode);
}
