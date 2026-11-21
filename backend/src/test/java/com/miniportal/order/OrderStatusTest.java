package com.miniportal.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Test
    void ak13_bestelltCanTransitionToInProduktion() {
        assertThat(OrderStatus.BESTELLT.canTransitionTo(OrderStatus.IN_PRODUKTION)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "BESTELLT,      BESTELLT,      false",
            "BESTELLT,      IN_PRODUKTION, true",
            "BESTELLT,      VERSENDET,     false",
            "IN_PRODUKTION, BESTELLT,      false",
            "IN_PRODUKTION, IN_PRODUKTION, false",
            "IN_PRODUKTION, VERSENDET,     true",
            "VERSENDET,     BESTELLT,      false",
            "VERSENDET,     IN_PRODUKTION, false",
            "VERSENDET,     VERSENDET,     false"
    })
    void ak14_allowsOnlyTransitionsFromStatusModel(OrderStatus from, OrderStatus to, boolean allowed) {
        assertThat(from.canTransitionTo(to)).isEqualTo(allowed);
    }
}
