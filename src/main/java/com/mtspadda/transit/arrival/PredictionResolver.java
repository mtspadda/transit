package com.mtspadda.transit.arrival;

import com.mtspadda.transit.realtime.StopUpdate;
import com.mtspadda.transit.realtime.TripRealtime;

public final class PredictionResolver {

    private PredictionResolver(){}

    static Prediction resolve(TripRealtime trip, int stopSequence, String stopId, long scheduledEpochSeconds){

        if (trip == null) return Prediction.NONE; // rule 1
        if (trip.canceled()) return new Prediction(ArrivalStatus.CANCELED, null); // rule 2

        Integer propagatedDelay = null;

        for (StopUpdate update : trip.stopUpdates()) {
            if (isSameStop(update, stopSequence, stopId)) {              // rule 3
                if (update.skipped()) {
                    return new Prediction(ArrivalStatus.SKIPPED, null);
                }
                Integer delay = delayOf(update, scheduledEpochSeconds);
                if (delay != null) {
                    return new Prediction(ArrivalStatus.REALTIME, delay);
                }
                break;
            }
            if (isEarlieStop(update, stopSequence)
                && !update.skipped()
                && update.arrivalDelaySeconds() != null) {

                propagatedDelay = update.arrivalDelaySeconds();
            }

        }

        return propagatedDelay != null ? new Prediction(ArrivalStatus.REALTIME, propagatedDelay) : Prediction.NONE;

    }

    private static boolean isSameStop(StopUpdate update, int stopSequence, String stopId){
        if (update.stopSequence() != null){
            return update.stopSequence() == stopSequence;
        }
        return stopId.equals(update.stopId());
    }

    private static boolean isEarlieStop(StopUpdate update, int stopSequence){
        return update.stopSequence() != null && update.stopSequence() < stopSequence;
    }

    private static Integer delayOf(StopUpdate update, long scheduledEpochSeconds){
        if (update.arrivalEpochSeconds() != null){
            return (int) (update.arrivalEpochSeconds() - scheduledEpochSeconds)
        }
        return update.arrivalDelaySeconds();
    }
}
