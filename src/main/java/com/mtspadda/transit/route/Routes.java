package com.mtspadda.transit.route;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "routes")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Routes {

    @Id
    private String routeId;

    private String agencyId;
    private String routeShortName;
    private String routeLongName;
    private Integer routeType;
}
