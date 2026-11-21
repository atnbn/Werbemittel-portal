package com.miniportal.order;

import com.miniportal.order.dto.CreateOrderRequest;
import com.miniportal.order.dto.OrderResponse;
import com.miniportal.order.dto.UpdateOrderStatusRequest;
import com.miniportal.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@AuthenticationPrincipal AuthenticatedUser user,
                                    @Valid @RequestBody CreateOrderRequest request) {
        return OrderResponse.from(orderService.placeOrder(
                user.getPartnerId(), request.templateId(), request.quantity(), request.values()));
    }

    @GetMapping
    public List<OrderResponse> findAll(@AuthenticationPrincipal AuthenticatedUser user) {
        return orderService.findVisibleOrders(user).stream()
                .map(OrderResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return OrderResponse.from(orderService.getVisibleOrder(id, user));
    }

    @PatchMapping("/{id}/status")
    public OrderResponse changeStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return OrderResponse.from(orderService.changeStatus(id, request.status()));
    }
}
