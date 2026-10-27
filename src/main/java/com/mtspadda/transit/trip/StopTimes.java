package com.mtspadda.transit.trip;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stop_times")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StopTimes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tripId;
    private String stopId;
    private Integer stopSequence;
    private Integer arrivalTime;
    private Integer departureTime;
    private String stopHeadsign;
    private Integer pickupType;
    private Integer dropOffType;

}
