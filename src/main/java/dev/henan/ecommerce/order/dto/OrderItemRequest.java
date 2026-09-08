package dev.henan.ecommerce.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(

        @NotNull(message = "O id do produto e obrigatorio.")
        Long productId,

        @NotNull(message = "A quantidade e obrigatoria.")
        @Min(value = 1, message = "A quantidade minima e 1.")
        @Max(value = 999, message = "A quantidade maxima por item e 999.")
        Integer quantity
) {
}
