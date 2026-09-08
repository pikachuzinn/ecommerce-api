package dev.henan.ecommerce.order.dto;

import dev.henan.ecommerce.order.Order;
import dev.henan.ecommerce.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

/** Projecao enxuta usada nas listagens, para nao trafegar o pedido inteiro. */
public record OrderSummaryResponse(
        Long id,
        String code,
        OrderStatus status,
        int totalItems,
        BigDecimal total,
        Instant createdAt
) {

    public static OrderSummaryResponse from(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getCode(),
                order.getStatus(),
                order.getTotalItems(),
                order.getTotal(),
                order.getCreatedAt());
    }
}
