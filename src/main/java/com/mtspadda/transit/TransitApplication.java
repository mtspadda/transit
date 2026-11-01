package com.mtspadda.transit;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling // without this @scheduled is ignored
public class TransitApplication {

	public static void main(String[] args) {

		SpringApplication.run(TransitApplication.class, args);
	}

}
