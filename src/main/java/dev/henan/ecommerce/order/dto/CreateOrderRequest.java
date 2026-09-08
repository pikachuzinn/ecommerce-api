package dev.henan.ecommerce.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(

        @NotEmpty(message = "O pedido precisa de ao menos um item.")
        @Size(max = 50, message = "Um pedido aceita no maximo 50 itens distintos.")
        @Valid
        List<OrderItemRequest> items,

        @NotNull(message = "O endereco de entrega e obrigatorio.")
        @Valid
        AddressRequest shippingAddress,

        @DecimalMin(value = "0.00", message = "O frete nao pode ser negativo.")
        BigDecimal shippingFee
) {
}
