package com.mtspadda.transit.stop;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stops")
@RequiredArgsConstructor
public class StopController {

    private final StopService service;

    @GetMapping("/nearby")
    List<NearbyStopResponse> nearby(
            @RequestParam @DecimalMin("-90") @DecimalMax("90") double lat,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") double lon,
            @RequestParam(defaultValue = "500") @Min(1) @Max(2000) int radius,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit){

        return service.findNearby(lat, lon, radius, limit);
    }

    @GetMapping
    List<StopResponse> findByCode(@RequestParam String code) {
        return service.findByCode(code);
    }
}
