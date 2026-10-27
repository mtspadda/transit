package com.mtspadda.transit.stop;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "stops")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Stops {

    @Id
    private String stopId;

    private String stopCode;
    private String stopName;
    private Double stopLat;
    private Double stopLon;
    //Location is not mapped;
}
