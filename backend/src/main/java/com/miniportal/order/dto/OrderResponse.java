package com.miniportal.order.dto;

import com.miniportal.order.Order;
import com.miniportal.order.OrderFieldValue;
import com.miniportal.order.OrderStatus;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public record OrderResponse(
        Long id,
        Long templateId,
        String templateName,
        Long partnerId,
        int quantity,
        OrderStatus status,
        long totalCents,
        long subsidyCents,
        long ownShareCents,
        Map<String, String> values,
        Instant createdAt,
        Instant updatedAt) {

    public static OrderResponse from(Order order) {
        Map<String, String> values = new LinkedHashMap<>();
        for (OrderFieldValue fieldValue : order.getFieldValues()) {
            values.put(fieldValue.getKey(), fieldValue.getValue());
        }
        return new OrderResponse(
                order.getId(),
                order.getTemplate().getId(),
                order.getTemplate().getName(),
                order.getPartner().getId(),
                order.getQuantity(),
                order.getStatus(),
                order.getTotalCents(),
                order.getSubsidyCents(),
                order.getOwnShareCents(),
                values,
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
