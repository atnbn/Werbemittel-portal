package com.miniportal.order;

public enum OrderStatus {
    BESTELLT,
    IN_PRODUKTION,
    VERSENDET;

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case BESTELLT -> target == IN_PRODUKTION;
            case IN_PRODUKTION -> target == VERSENDET;
            case VERSENDET -> false;
        };
    }
}
