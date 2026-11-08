package com.mtspadda.transit.realtime;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.transit.realtime.GtfsRealtime;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
class GtfsRealtimeClient {

    private final RestClient restClient;
    private final RealtimeProperties properties;

    GtfsRealtimeClient(RealtimeProperties properties) {
        this.properties = properties;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(30));

        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    FeedMessage fetchTripUpdates(){
        byte[] body = restClient.get().uri(properties.url())
                .header("x-api-key", properties.apiKey())
                .retrieve()
                .body(byte[].class);

        try {
            return FeedMessage.parseFrom(body);
        } catch (InvalidProtocolBufferException e){
            throw new IllegalStateException("Realtime feed is not valid GTFS-Realtime", e);
        }
    }
}
