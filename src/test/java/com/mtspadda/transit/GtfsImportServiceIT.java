package com.mtspadda.transit;

import com.mtspadda.transit.gtfs.GtfsImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.CharSetUtils.count;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
public class GtfsImportServiceIT {

    private static final List<String> FILES =
            List.of("routes.txt", "stops.txt", "trips.txt", "stop_times.txt");

    @Autowired
    GtfsImportService service;

    @Autowired
    JdbcTemplate jdbc;

    @TempDir
    Path tempDir;

    private Path sampleZip(Map<String, String> overrides) {
        Path zip = tempDir.resolve(UUID.randomUUID() + ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            for (String name : FILES) {
                out.putNextEntry(new ZipEntry(name));
                out.write(overrides.containsKey(name)
                        ? overrides.get(name).getBytes(UTF_8)
                        : readSample(name));
                out.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return zip;
    }

    private byte[] readSample(String name) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/gtfs/sample/" + name)) {
            if (in == null) {
                throw new IllegalStateException("Missing sample file: " + name);
            }
            return in.readAllBytes();
        }
    }

    @Test
    void importAllFiles(){
        service.importZip(sampleZip(Map.of()));

        assertThat(count("routes")).isEqualTo(1);
        assertThat(count("stops")).isEqualTo(3);
        assertThat(count("trips")).isEqualTo(2);
        assertThat(count("stop_times")).isEqualTo(5);
    }

    @Test
    void storesTimesAfterMidnightAsSeconds(){
        service.importZip(sampleZip(Map.of()));

        Integer arrival = jdbc.queryForObject(
                "SELECT arrival_time FROM stop_times WHERE trip_id = 'T2' AND stop_sequence = 2",
                Integer.class
        );

        assertThat(arrival).isEqualTo(86_700);
    }

    @Test
    void fillGeographicLocation(){
        service.importZip(sampleZip(Map.of()));

        Double lat = jdbc.queryForObject(
                "SELECT ST_Y(location::geometry) FROM stops WHERE stop_id = 'TEST_0001'", Double.class);
        Double lon = jdbc.queryForObject(
                "SELECT ST_X(location::geometry) FROM stops WHERE stop_id = 'TEST_0001'", Double.class);

        assertThat(lat).isCloseTo(53.351, within(0.000001));
        assertThat(lon).isCloseTo(-6.2607, within(0.000001));

    }

    @Test
    void reimportReplacePreviousData(){
        service.importZip(sampleZip(Map.of()));
        service.importZip(sampleZip(Map.of()));

        assertThat(count("stops")).isEqualTo(3);
        assertThat(count("stop_times")).isEqualTo(5);

    }

}
