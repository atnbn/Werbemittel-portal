package com.miniportal.order;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PriceCalculatorTest {

    private final PriceCalculator priceCalculator = new PriceCalculator();

    @Test
    void ak3_calculatesExample1() {
        PriceBreakdown price = priceCalculator.calculate(500, 12, 50);

        assertThat(price).isEqualTo(new PriceBreakdown(6_000, 3_000, 3_000));
    }

    @Test
    void ak3_roundsSubsidyDownExample2() {
        PriceBreakdown price = priceCalculator.calculate(333, 12, 33);

        assertThat(price).isEqualTo(new PriceBreakdown(3_996, 1_318, 2_678));
    }
}
