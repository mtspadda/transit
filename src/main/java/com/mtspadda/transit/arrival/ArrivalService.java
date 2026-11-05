package com.mtspadda.transit.arrival;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArrivalService {

    private static final ZoneId DUBLIN = ZoneId.of("Europe/Dublin");
    private static final int SECONDS_PER_DAY = 86_400;

    private final ArrivalRepository repository;
    private final Clock clock;

    public List<ArrivalResponse> scheduledArrivals(String stopId, int minutes) {
        ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(DUBLIN);
        LocalDate today = now.toLocalDate();
        int nowSeconds = now.toLocalTime().toSecondOfDay();
        int windowEnd = nowSeconds + minutes * 60;

        List<ArrivalResponse> arrivals = new ArrayList<>();

        //Today = 0; Yesterday = -1; Tomorrow = 1
        for (int dayOffset = -1; dayOffset <= 1; dayOffset++) {
            LocalDate serviceDate = today.plusDays(dayOffset);
            int shift = dayOffset * SECONDS_PER_DAY;
            int from = nowSeconds - shift;
            int to = windowEnd - shift;

            if (to < 0) {
                continue; //window doenst get to this service day
            }
            for (ScheduledStopTime stopTime : repository.findScheduled(stopId, serviceDate, Math.max(from, 0), to)) {
                ZonedDateTime arrivalAt = serviceDayStart(serviceDate).plusSeconds(stopTime.arrivalSeconds());
                arrivals.add(new ArrivalResponse(
                        stopTime.tripId(),
                        stopTime.routeName(),
                        stopTime.headsign(),
                        arrivalAt.toOffsetDateTime(),
                        Duration.between(now, arrivalAt).toMinutes()));
            }
        }

        arrivals.sort(Comparator.comparing(ArrivalResponse::scheduledAt));
        return arrivals;
    }


    private static ZonedDateTime serviceDayStart(LocalDate serviceDate) {
        return serviceDate.atTime(LocalTime.NOON).atZone(DUBLIN).minusHours(12);
    }
}
