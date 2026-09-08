package dev.henan.ecommerce.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * O frete nao aparece aqui de proposito: quem calcula o valor cobrado e o
 * servidor ({@code ShippingCalculator}), nunca o cliente.
 */
public record CreateOrderRequest(

        @NotEmpty(message = "O pedido precisa de ao menos um item.")
        @Size(max = 50, message = "Um pedido aceita no maximo 50 itens distintos.")
        @Valid
        List<OrderItemRequest> items,

        @NotNull(message = "O endereco de entrega e obrigatorio.")
        @Valid
        AddressRequest shippingAddress
) {
}
