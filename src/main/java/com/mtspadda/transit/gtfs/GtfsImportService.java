package com.mtspadda.transit.gtfs;

import com.mtspadda.transit.gtfs.row.RouteRow;
import com.mtspadda.transit.gtfs.row.StopRow;
import com.mtspadda.transit.gtfs.row.StopTimeRow;
import com.mtspadda.transit.gtfs.row.TripRow;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class GtfsImportService {

    private static final Logger log = LoggerFactory.getLogger(GtfsImportService.class);
    private static final int BATCH_SIZE = 5_000;

    private final GtfsDownloader downloader;
    private final GtfsCsvReader reader;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;

    public GtfsImportService(GtfsDownloader downloader, GtfsCsvReader reader,
                             JdbcTemplate jdbc, PlatformTransactionManager transactionManager) {
        this.downloader = downloader;
        this.reader = reader;
        this.jdbc = jdbc;
        this.transaction = new TransactionTemplate(transactionManager);
    }

    /** Baixa o feed e importa. É o método que o job vai chamar. */
    public void importLatestFeed() {
        Path zip = downloader.download();          // fora da transação
        try {
            importZip(zip);
        } finally {
            deleteQuietly(zip);
        }
    }

    /** Importa um zip já baixado. Separado para ser testado sem internet. */
    public void importZip(Path zipPath) {
        long start = System.currentTimeMillis();

        transaction.executeWithoutResult(status -> {
            try (ZipFile zip = new ZipFile(zipPath.toFile())) {
                clearTables();

                // Ordem importa por causa das foreign keys
                importFile(zip, "routes.txt", RouteRow::from, """
                        INSERT INTO routes (route_id, agency_id, route_short_name, route_long_name, route_type)
                        VALUES (?, ?, ?, ?, ?)""",
                        (ps, r) -> {
                            ps.setString(1, r.routeId());
                            ps.setString(2, r.agencyId());
                            ps.setString(3, r.routeShortName());
                            ps.setString(4, r.routeLongName());
                            ps.setObject(5, r.routeType(), Types.SMALLINT);
                        });

                importFile(zip, "stops.txt", StopRow::from, """
                        INSERT INTO stops (stop_id, stop_code, stop_name, stop_lat, stop_lon)
                        VALUES (?, ?, ?, ?, ?)""",
                        (ps, s) -> {
                            ps.setString(1, s.stopId());
                            ps.setString(2, s.stopCode());
                            ps.setString(3, s.stopName());
                            ps.setDouble(4, s.stopLat());
                            ps.setDouble(5, s.stopLon());
                        });

                importFile(zip, "trips.txt", TripRow::from, """
                        INSERT INTO trips (trip_id, route_id, service_id, trip_headsign, direction_id)
                        VALUES (?, ?, ?, ?, ?)""",
                        (ps, t) -> {
                            ps.setString(1, t.tripId());
                            ps.setString(2, t.routeId());
                            ps.setString(3, t.serviceId());
                            ps.setString(4, t.tripHeadsign());
                            ps.setObject(5, t.directionId(), Types.SMALLINT);
                        });

                importFile(zip, "stop_times.txt", StopTimeRow::from, """
                        INSERT INTO stop_times (trip_id, stop_id, stop_sequence, arrival_time, departure_time,
                                                stop_headsign, pickup_type, drop_off_type)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
                        (ps, st) -> {
                            ps.setString(1, st.tripId());
                            ps.setString(2, st.stopId());
                            ps.setInt(3, st.stopSequence());
                            ps.setObject(4, st.arrivalTime(), Types.INTEGER);
                            ps.setObject(5, st.departureTime(), Types.INTEGER);
                            ps.setString(6, st.stopHeadsign());
                            ps.setObject(7, st.pickupType(), Types.SMALLINT);
                            ps.setObject(8, st.dropOffType(), Types.SMALLINT);
                        });

            } catch (IOException e) {
                throw new UncheckedIOException("Failed to open GTFS zip", e);
            }
        });

        log.info("GTFS import finished in {} s", (System.currentTimeMillis() - start) / 1000);
    }

    private void clearTables() {
        // Ordem inversa das foreign keys: filhos antes dos pais
        jdbc.update("DELETE FROM stop_times");
        jdbc.update("DELETE FROM trips");
        jdbc.update("DELETE FROM stops");
        jdbc.update("DELETE FROM routes");
    }

    private <T> void importFile(ZipFile zip, String fileName, Function<CSVRecord, T> mapper,
                                String sql, ParameterizedPreparedStatementSetter<T> setter) {
        ZipEntry entry = zip.getEntry(fileName);
        if (entry == null) {
            throw new IllegalStateException("GTFS zip has no " + fileName);
        }

        BatchWriter<T> writer = new BatchWriter<>(sql, setter);
        try (InputStream input = zip.getInputStream(entry)) {
            reader.read(input, mapper, writer);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + fileName, e);
        }
        writer.flush();

        log.info("Imported {} rows from {}", writer.total, fileName);
    }

    /** Junta as linhas em lotes e manda cada lote ao banco de uma vez. */
    private final class BatchWriter<T> implements Consumer<T> {
        private final String sql;
        private final ParameterizedPreparedStatementSetter<T> setter;
        private final List<T> batch = new ArrayList<>(BATCH_SIZE);
        private long total;

        BatchWriter(String sql, ParameterizedPreparedStatementSetter<T> setter) {
            this.sql = sql;
            this.setter = setter;
        }

        @Override
        public void accept(T row) {
            batch.add(row);
            if (batch.size() == BATCH_SIZE) {
                flush();
            }
        }

        void flush() {
            if (batch.isEmpty()) {
                return;
            }
            jdbc.batchUpdate(sql, batch, batch.size(), setter);
            total += batch.size();
            batch.clear();
        }
    }

    private static void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Could not delete temp file {}", file, e);
        }
    }
}