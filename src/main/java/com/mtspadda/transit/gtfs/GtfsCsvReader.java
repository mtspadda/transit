package com.mtspadda.transit.gtfs;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class GtfsCsvReader {

    private static final char BOM = '\uFEFF';

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setTrim(true)
            .get();

    public <T> void read(InputStream input, Function<CSVRecord, T> mapper, Consumer<T> consumer) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
             CSVParser parser = FORMAT.parse(skipBom(reader))) {

            for (CSVRecord record : parser) {
                T row;
                try {
                    row = mapper.apply(record);
                } catch (RuntimeException e) {
                    throw new IllegalStateException(
                            "Failed to parse line " + record.getRecordNumber() + ": " + record, e);
                }
                consumer.accept(row);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read GTFS file", e);
        }
    }

    private static BufferedReader skipBom(BufferedReader reader) throws IOException {
        reader.mark(1);              // marca a posição atual
        if (reader.read() != BOM) {  // lê o primeiro caractere
            reader.reset();          // não era BOM: volta para a marca
        }
        return reader;
    }
}
