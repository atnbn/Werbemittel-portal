package com.miniportal.order;

public class PriceCalculator {

    private static final int PERCENT = 100;

    public PriceBreakdown calculate(int quantity, long unitPriceCents, int subsidyPercent) {
        long totalCents = quantity * unitPriceCents;
        long subsidyCents = totalCents * subsidyPercent / PERCENT;
        return new PriceBreakdown(totalCents, subsidyCents, totalCents - subsidyCents);
    }
}
