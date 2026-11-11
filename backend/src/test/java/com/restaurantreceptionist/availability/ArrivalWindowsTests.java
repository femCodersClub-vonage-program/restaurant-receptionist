package com.restaurantreceptionist.availability;

import com.restaurantreceptionist.reservations.DiningSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ArrivalWindowsTests {

    @ParameterizedTest
    @CsvSource({
            "13:00, LUNCH",
            "14:30, LUNCH",
            "16:00, LUNCH",
            "20:00, DINNER",
            "21:45, DINNER",
            "23:30, DINNER"
    })
    void classifiesTimesInsideInclusiveWindows(LocalTime arrivalTime, DiningSession expected) {
        assertThat(ArrivalWindows.classify(arrivalTime)).contains(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = { "00:00", "12:59", "16:01", "17:30", "19:59", "23:31", "23:59" })
    void rejectsTimesOutsideInclusiveWindows(LocalTime arrivalTime) {
        assertThat(ArrivalWindows.classify(arrivalTime)).isEmpty();
    }

    @Test
    void rejectsNullArrivalTime() {
        assertThatNullPointerException().isThrownBy(() -> ArrivalWindows.classify(null));
    }
}
