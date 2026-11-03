package com.mtspadda.transit.stop;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(StopController.class)
public class StopControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    StopService service;

    @Test
    void returnsNearbyStopsUsingDefaults() throws Exception {
        when(service.findNearby(53.3498, -6.2603, 500, 20)).thenReturn(List.of(
                new NearbyStopResponse("TEST_0001", "101", "O'Connell Street Upper", 53.351, -6.2607, 136)));

        mvc.perform(get("/api/stops/nearby")
                        .param("lat", "53.3498")
                        .param("lon", "-6.2603"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("TEST_0001"))
                .andExpect(jsonPath("$[0].name").value("O'Connell Street Upper"))
                .andExpect(jsonPath("$[0].distanceMeters").value(136));
    }

    @Test
    void rejectsLatitudeOutOfRange() throws Exception {
        mvc.perform(get("/api/stops/nearby")
                        .param("lat", "95")
                        .param("lon", "-6.2603"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.lat").exists());

        verifyNoInteractions(service);
    }

    @Test
    void rejectsRadiusAboveMaximum() throws Exception {
        mvc.perform(get("/api/stops/nearby")
                        .param("lat", "53.3498")
                        .param("lon", "-6.2603")
                        .param("radius", "5000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.radius").exists());

        verifyNoInteractions(service);
    }

    @Test
    void rejectsMissingLatitude() throws Exception {
        mvc.perform(get("/api/stops/nearby")
                        .param("lon", "-6.2603"))
                .andExpect(status().isBadRequest());
    }
}
