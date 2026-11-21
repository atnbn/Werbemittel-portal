package com.miniportal.order.dto;

import com.miniportal.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Status fehlt.")
        OrderStatus status) {
}
