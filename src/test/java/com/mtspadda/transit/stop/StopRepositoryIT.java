package com.mtspadda.transit.stop;
import com.mtspadda.transit.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class StopRepositoryIT {

    private static final double SPIRE_LAT = 53.3498;
    private static final double SPIRE_LON = -6.2603;

    @Autowired
    StopRepository repository;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        // Outros testes podem ter deixado dados; apaga respeitando as foreign keys
        jdbc.update("DELETE FROM stop_times");
        jdbc.update("DELETE FROM trips");
        jdbc.update("DELETE FROM stops");

        insertStop("TEST_0001", "O'Connell Street Upper", 53.351, -6.2607);   // ~136 m
        insertStop("TEST_0002", "Parnell Square West", 53.3522, -6.2637);     // ~350 m
        insertStop("TEST_0003", "Dame Street", 53.3441, -6.2615);             // ~639 m
    }

    @Test
    void returnsOnlyStopsInsideRadiusOrderedByDistance() {
        List<NearbyStop> result = repository.findNearby(SPIRE_LAT, SPIRE_LON, 400, 10);

        assertThat(result)
                .extracting(NearbyStop::getStopId)
                .containsExactly("TEST_0001", "TEST_0002");
    }

    @Test
    void returnsDistanceInMeters() {
        List<NearbyStop> result = repository.findNearby(SPIRE_LAT, SPIRE_LON, 400, 10);

        assertThat(result.getFirst().getDistanceMeters()).isCloseTo(136.0, within(3.0));
    }

    @Test
    void respectsLimit() {
        List<NearbyStop> result = repository.findNearby(SPIRE_LAT, SPIRE_LON, 1000, 2);

        assertThat(result)
                .extracting(NearbyStop::getStopId)
                .containsExactly("TEST_0001", "TEST_0002");
    }

    @Test
    void returnsEmptyWhenNothingIsNearby() {
        assertThat(repository.findNearby(SPIRE_LAT, SPIRE_LON, 50, 10)).isEmpty();
    }

    private void insertStop(String id, String name, double lat, double lon) {
        jdbc.update("INSERT INTO stops (stop_id, stop_name, stop_lat, stop_lon) VALUES (?, ?, ?, ?)",
                id, name, lat, lon);
    }
}