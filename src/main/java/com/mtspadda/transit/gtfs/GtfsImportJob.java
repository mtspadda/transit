package com.mtspadda.transit.gtfs;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor // Uses only final fields
public class GtfsImportJob implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(GtfsImportJob.class);

    private final GtfsImportService service;
    private final GtfsProperties properties;
    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args){
        if (!properties.importOnStartup()) {
            log.info("GTFS import on startup is disabled!");
            return;
        }

        if(hasData()){
            log.info("GTFS data already present, skipping startup import");
            return;
        }

        log.info("Database os empty, runing initial GTFS import!");
        runImport();
    }

    @Scheduled(cron = "${gtfs.import-cron}", zone = "Europe/Dublin")
    void scheduledImport(){
        log.info("Starting scheduled GTFS import");
        runImport();
    }


    private boolean hasData() {
        Boolean exists = jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM stops)", Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    private void runImport() {
        try {
            service.importLatestFeed();
        } catch (RuntimeException e) {
            log.error("GTFS import failed, keeping previous data", e);
        }
    }
}
