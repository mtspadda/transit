package com.mtspadda.transit;

import com.mtspadda.transit.gtfs.GtfsDownloader;
import com.mtspadda.transit.gtfs.GtfsImportService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication @ConfigurationPropertiesScan
public class TransitApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransitApplication.class, args);
	}
/**
	@Bean
	ApplicationRunner testImport(GtfsImportService importService) {
		return args -> importService.importLatestFeed();
	}
**/
}
