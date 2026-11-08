package com.mtspadda.transit.realtime;


import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class RealtimeStore {

    private final AtomicReference<RealtimeSnapshot> current =
            new AtomicReference<>(RealtimeSnapshot.empty());

    public RealtimeSnapshot current() {
        return current.get();
    }

    void replace(RealtimeSnapshot snap){
        current.set(snap);
    }
}
