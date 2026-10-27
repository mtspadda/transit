package com.mtspadda.transit;

import org.springframework.boot.SpringApplication;

public class TestTransitApplication {

	public static void main(String[] args) {
		SpringApplication.from(TransitApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
