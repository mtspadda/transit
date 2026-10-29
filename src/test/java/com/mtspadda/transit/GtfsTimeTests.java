package com.mtspadda.transit;
import com.mtspadda.transit.gtfs.GtfsTime;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


class GtfsTimeTest {

@Test
void convertsRegularTime() {
    assertThat(GtfsTime.toSeconds("08:15:30")).isEqualTo(29730);
}

@Test
void convertsTimeAfterMidnightOfServiceDay() {
    assertThat(GtfsTime.toSeconds("24:20:00")).isEqualTo(87600);
}

@Test
void returnsNullForBlankValue() {
    assertThat(GtfsTime.toSeconds("")).isNull();
    assertThat(GtfsTime.toSeconds("   ")).isNull();
    assertThat(GtfsTime.toSeconds(null)).isNull();
}

@Test
void returnsNullForInvalidValue() {
    assertThatThrownBy(() -> GtfsTime.toSeconds("abc"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("abc");
}
}