package com.mtspadda.transit.realtime;

import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "gtfs.realtime", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class RealtimePoller {

    private static final Logger log = LoggerFactory.getLogger(RealtimePoller.class);
    private final GtfsRealtimeClient cliente;
    private final RealtimeStore store;

    @Scheduled(fixedDelayString = "${gtfs.realtime.poll-interval-ms}", initialDelay = 5_000)
    void poll(){
        try {
            RealtimeSnapshot snapshot = toSnapshot(cliente.fetchTripUpdates());
            store.replace(snapshot);

            long canceled = snapshot.trips().values().stream()
                    .filter(TripRealtime::canceled)
                    .count();
            log.info("Realtime feed: {} trips ({} canceled), generated {} s ago",
                    snapshot.trips().size(), canceled,
                    Duration.between(snapshot.feedTimestamp(), Instant.now()).toSeconds());
        } catch (RuntimeException e) {
            log.warn("Realtime poll failed, keeping previous data: {}", e.getMessage());
        }
    }

    static RealtimeSnapshot toSnapshot(FeedMessage feed){
        Map<String, TripRealtime> trips = new HashMap<>();

        for(FeedEntity entity : feed.getEntityList()){
            if (!entity.hasTripUpdate()){
                continue;
            }

            TripUpdate tripUpdate = entity.getTripUpdate();
            String tripId = tripUpdate.getTrip().getTripId();
            boolean canceled = tripUpdate.getTrip().getScheduleRelationship()
                    == GtfsRealtime.TripDescriptor.ScheduleRelationship.CANCELED;

            List<StopUpdate> stopUpdates = tripUpdate.getStopTimeUpdateList()
                    .stream()
                    .map(RealtimePoller::toStopUpdate)
                    .toList();

            trips.put(tripId, new TripRealtime(tripId, canceled, stopUpdates));

        }

        return new RealtimeSnapshot(
                Instant.ofEpochSecond(feed.getHeader().getTimestamp()),
                Map.copyOf(trips)
        );
    }

    private static StopUpdate toStopUpdate(TripUpdate.StopTimeUpdate update) {
        boolean hasArrival = update.hasArrival();
        return new StopUpdate(
                update.hasStopSequence() ? update.getStopSequence() : null,
                update.hasStopId() ? update.getStopId() : null,
                hasArrival && update.getArrival().hasDelay() ? update.getArrival().getDelay() : null,
                hasArrival && update.getArrival().hasTime() ? update.getArrival().getTime() : null,
                update.getScheduleRelationship() == TripUpdate.StopTimeUpdate.ScheduleRelationship.SKIPPED);
    }
}
