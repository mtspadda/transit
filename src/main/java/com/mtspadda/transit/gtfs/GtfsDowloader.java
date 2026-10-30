package com.mtspadda.transit.gtfs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

@Component
public class GtfsDowloader {

    private static final Logger log = LoggerFactory.getLogger(GtfsDowloader.class);

    private final RestClient restClient;
    private final GtfsProperties properties;

    public GtfsDowloader(GtfsProperties properties){
        this.properties = properties;


        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMinutes(5));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

     public Path download(){
        log.info("Downloading GTFS from {}", properties.staticUrl());
        try {
            Path target = Files.createTempFile("gtfs-", ".zip");

            restClient.get()
                    .uri(properties.staticUrl())
                    .exchange((request, response)->{
                        if (!response.getStatusCode().is2xxSuccessful()){
                            throw new IllegalStateException(
                                    "GTFS download failed with status:" + response.getStatusCode()
                            );
                        }
                        Files.copy(response.getBody(), target, StandardCopyOption.REPLACE_EXISTING);
                        return null;
                    });

            log.info("GTFS downloaded: {} MB", Files.size(target) / 1_048_576);
            return target;
        } catch (IOException e){
            throw new UncheckedIOException("Failed to download GTFS", e);
        }
     }
}
