package com.mtspadda.transit.arrival;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stops")
@RequiredArgsConstructor
public class ArrivalController {

    private final ArrivalService service;

    @GetMapping("/{stopId}/arrivals")
    List<ArrivalResponse> arrivals(
            @PathVariable String stopId,
            @RequestParam(defaultValue = "60") @Min(1) @Max(180) int minutes){

        return service.scheduledArrivals(stopId, minutes);
    }

}
