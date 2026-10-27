package com.mtspadda.transit.trip;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Trips {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String tripId;

    private String routeId;
    private String serviceId;
    private String tripHeadsign;
    private Integer directionId;

}
