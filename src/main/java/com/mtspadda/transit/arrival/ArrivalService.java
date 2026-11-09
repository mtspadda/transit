package com.mtspadda.transit.arrival;

import com.mtspadda.transit.realtime.RealtimeSnapshot;
import com.mtspadda.transit.realtime.RealtimeStore;
import com.mtspadda.transit.realtime.TripRealtime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ArrivalService {

    private static final ZoneId DUBLIN = ZoneId.of("Europe/Dublin");
    private static final int SECONDS_PER_DAY = 86_400;
    private static final int LOOKBACK_SECONDS = 30 * 60;
    private static final Duration MAX_FEED_AGE = Duration.ofMinutes(5);

    private final ArrivalRepository repository;
    private final RealtimeStore store;
    private final Clock clock;
    public List<ArrivalResponse> nextArrivals(String stopId, int minutes) {
        ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(DUBLIN);
        Map<String, TripRealtime> realtime = freshRealtime(now);

        LocalDate today = now.toLocalDate();
        int nowSeconds = now.toLocalTime().toSecondOfDay();
        int windowEnd = nowSeconds + minutes * 60;

        List<ArrivalResponse> arrivals = new ArrayList<>();

        for (int dayOffset = -1; dayOffset <= 1; dayOffset++) {
            LocalDate serviceDate = today.plusDays(dayOffset);
            int shift = dayOffset * SECONDS_PER_DAY;
            int from = nowSeconds - LOOKBACK_SECONDS - shift;
            int to = windowEnd - shift;

            if (to < 0) {
                continue;
            }

            for (ScheduledStopTime stopTime : repository.findScheduled(stopId, serviceDate, Math.max(from, 0), to)) {
                ZonedDateTime scheduledAt = serviceDayStart(serviceDate).plusSeconds(stopTime.arrivalSeconds());

                Prediction prediction = PredictionResolver.resolve(
                        realtime.get(stopTime.tripId()), stopTime.stopSequence(), stopId, scheduledAt.toEpochSecond());

                ZonedDateTime expectedAt = prediction.delaySeconds() != null
                        ? scheduledAt.plusSeconds(prediction.delaySeconds())
                        : null;
                ZonedDateTime effectiveAt = expectedAt != null ? expectedAt : scheduledAt;

                if (effectiveAt.isBefore(now)) {
                    continue;   // already pass
                }

                arrivals.add(new ArrivalResponse(
                        stopTime.tripId(),
                        stopTime.routeName(),
                        stopTime.headsign(),
                        scheduledAt.toOffsetDateTime(),
                        expectedAt != null ? expectedAt.toOffsetDateTime() : null,
                        prediction.delaySeconds(),
                        prediction.status(),
                        Duration.between(now, effectiveAt).toMinutes()));
            }
        }

        arrivals.sort(Comparator.comparing(ArrivalService::effectiveAt));
        return arrivals;
    }


    private Map<String, TripRealtime> freshRealtime(ZonedDateTime now) {
        RealtimeSnapshot snapshot = store.current();
        Duration age = Duration.between(snapshot.feedTimestamp(), now.toInstant());
        return age.compareTo(MAX_FEED_AGE) > 0 ? Map.of() : snapshot.trips();
    }

    private static OffsetDateTime effectiveAt(ArrivalResponse arrival) {
        return arrival.expectedAt() != null ? arrival.expectedAt() : arrival.scheduledAt();
    }

    private static ZonedDateTime serviceDayStart(LocalDate serviceDate) {
        return serviceDate.atTime(LocalTime.NOON).atZone(DUBLIN).minusHours(12);
    }
}