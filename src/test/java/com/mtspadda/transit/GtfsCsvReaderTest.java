package com.mtspadda.transit;

import com.mtspadda.transit.gtfs.GtfsCsvReader;
import com.mtspadda.transit.gtfs.row.StopRow;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GtfsCsvReaderTest {

    private final GtfsCsvReader reader = new GtfsCsvReader();

    @Test
    void readsAllRowsFromFile() {
        InputStream input = getClass().getResourceAsStream("/gtfs/sample/stops.txt");
        assertThat(input).as("arquivo de teste não encontrado").isNotNull();

        List<StopRow> stops = readStops(input);

        assertThat(stops)
                .extracting(StopRow::stopId)
                .containsExactly("TEST_0001", "TEST_0002", "TEST_0003");

        StopRow first = stops.getFirst();
        assertThat(first.stopCode()).isEqualTo("101");
        assertThat(first.stopName()).isEqualTo("O'Connell Street Upper");
        assertThat(first.stopLat()).isEqualTo(53.351);
        assertThat(first.stopLon()).isEqualTo(-6.2607);
    }

    @Test
    void handlesQuotedValueWithComma() {
        List<StopRow> stops = readStops(getClass().getResourceAsStream("/gtfs/sample/stops.txt"));

        assertThat(stops.get(2).stopName()).isEqualTo("Dame Street, Trinity");
    }

    @Test
    void treatsEmptyOptionalValueAsNull() {
        List<StopRow> stops = readStops(getClass().getResourceAsStream("/gtfs/sample/stops.txt"));

        assertThat(stops.get(2).stopCode()).isNull();
    }

    @Test
    void treatsMissingOptionalColumnAsNull() {
        String csv = """
                stop_id,stop_name,stop_lat,stop_lon
                TEST_0001,Some Stop,53.35,-6.26
                """;

        List<StopRow> stops = readStops(toStream(csv));

        assertThat(stops).hasSize(1);
        assertThat(stops.getFirst().stopCode()).isNull();
    }

    @Test
    void skipsUtf8Bom() {
        String csv = "\uFEFF" + """
                stop_id,stop_name,stop_lat,stop_lon
                TEST_0001,Some Stop,53.35,-6.26
                """;

        List<StopRow> stops = readStops(toStream(csv));

        assertThat(stops.getFirst().stopId()).isEqualTo("TEST_0001");
    }

    @Test
    void returnsNothingForHeaderOnlyFile() {
        String csv = "stop_id,stop_name,stop_lat,stop_lon\n";

        assertThat(readStops(toStream(csv))).isEmpty();
    }

    @Test
    void reportsWhichLineFailed() {
        String csv = """
                stop_id,stop_name,stop_lat,stop_lon
                TEST_0001,Good Stop,53.35,-6.26
                TEST_0002,Bad Stop,abc,-6.26
                """;

        assertThatThrownBy(() -> readStops(toStream(csv)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to parse line")
                .hasMessageContaining("TEST_0002")
                .hasCauseInstanceOf(NumberFormatException.class);
    }

    private List<StopRow> readStops(InputStream input) {
        List<StopRow> stops = new ArrayList<>();
        reader.read(input, StopRow::from, stops::add);
        return stops;
    }

    private static InputStream toStream(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }
}