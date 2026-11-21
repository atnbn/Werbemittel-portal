package com.miniportal.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record CreateOrderRequest(
        @NotNull(message = "templateId fehlt.")
        Long templateId,

        @NotNull(message = "Menge fehlt.")
        @Min(value = 1, message = "Menge muss mindestens 1 sein.")
        Integer quantity,

        Map<String, @NotNull(message = "Werte in values dürfen nicht null sein.") String> values) {

    public CreateOrderRequest {
        values = values == null ? Map.of() : values;
    }
}
