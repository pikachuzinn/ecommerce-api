package dev.henan.ecommerce.order.dto;

import dev.henan.ecommerce.order.Address;
import dev.henan.ecommerce.order.Order;
import dev.henan.ecommerce.order.OrderItem;
import dev.henan.ecommerce.order.OrderStatus;
import dev.henan.ecommerce.order.payment.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record OrderResponse(
        Long id,
        String code,
        String customerName,
        String customerEmail,
        OrderStatus status,
        List<OrderStatus> allowedNextStates,
        AddressResponse shippingAddress,
        List<ItemResponse> items,
        BigDecimal itemsTotal,
        BigDecimal shippingFee,
        BigDecimal total,
        PaymentResponse payment,
        Instant createdAt,
        Instant updatedAt
) {

    public record ItemResponse(
            Long productId,
            String sku,
            String name,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
        static ItemResponse from(OrderItem item) {
            return new ItemResponse(
                    item.getProduct().getId(),
                    item.getProductSku(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal());
        }
    }

    public record AddressResponse(
            String street, String number, String complement,
            String district, String city, String state, String zipCode
    ) {
        static AddressResponse from(Address address) {
            if (address == null) {
                return null;
            }
            return new AddressResponse(
                    address.getStreet(), address.getNumber(), address.getComplement(),
                    address.getDistrict(), address.getCity(), address.getState(), address.getZipCode());
        }
    }

    public record PaymentResponse(
            String method,
            String description,
            Instant paidAt,
            LocalDate boletoDueDate
    ) {
        static PaymentResponse from(Payment payment) {
            if (payment == null) {
                return null;
            }
            LocalDate dueDate = payment instanceof dev.henan.ecommerce.order.payment.BoletoPayment boleto
                    ? boleto.getDueDate()
                    : null;
            return new PaymentResponse(
                    payment.getMethod().name(),
                    payment.getDescription(),
                    payment.getPaidAt(),
                    dueDate);
        }
    }

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCode(),
                order.getUser().getName(),
                order.getUser().getEmail(),
                order.getStatus(),
                List.copyOf(order.getStatus().nextStates()),
                AddressResponse.from(order.getShippingAddress()),
                order.getItems().stream().map(ItemResponse::from).toList(),
                order.getItemsTotal(),
                order.getShippingFee(),
                order.getTotal(),
                Optional.ofNullable(order.getPayment()).map(PaymentResponse::from).orElse(null),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
