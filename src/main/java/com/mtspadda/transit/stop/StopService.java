package com.mtspadda.transit.stop;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StopService {

    private final StopRepository repository;

    public List<NearbyStopResponse> findNearby(double lat, double lon, int radiusMeters, int limit){
        return repository.findNearby(lat, lon, radiusMeters, limit).stream()
                .map(NearbyStopResponse::from)
                .toList();
    }
}
