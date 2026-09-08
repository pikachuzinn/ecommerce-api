package dev.henan.ecommerce.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Maquina de estados do pedido")
class OrderStatusTest {

    @ParameterizedTest(name = "{0} -> {1} deve ser permitido")
    @CsvSource({
            "PENDING_PAYMENT, PAID",
            "PENDING_PAYMENT, CANCELED",
            "PAID,            SHIPPED",
            "PAID,            CANCELED",
            "SHIPPED,         DELIVERED"
    })
    void devePermitirTransicoesValidas(OrderStatus from, OrderStatus to) {
        assertThat(from.canTransitionTo(to)).isTrue();
    }

    @ParameterizedTest(name = "{0} -> {1} deve ser bloqueado")
    @CsvSource({
            "PENDING_PAYMENT, SHIPPED",
            "PENDING_PAYMENT, DELIVERED",
            "PAID,            DELIVERED",
            "SHIPPED,         CANCELED",
            "DELIVERED,       CANCELED",
            "CANCELED,        PAID",
            "PAID,            PENDING_PAYMENT"
    })
    void deveBloquearTransicoesInvalidas(OrderStatus from, OrderStatus to) {
        assertThat(from.canTransitionTo(to)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void nenhumStatusPodeTransicionarParaEleMesmo(OrderStatus status) {
        assertThat(status.canTransitionTo(status)).isFalse();
    }

    @Test
    @DisplayName("DELIVERED e CANCELED sao estados finais")
    void estadosFinaisNaoTemSaida() {
        assertThat(OrderStatus.DELIVERED.isFinal()).isTrue();
        assertThat(OrderStatus.CANCELED.isFinal()).isTrue();
        assertThat(OrderStatus.PENDING_PAYMENT.isFinal()).isFalse();
    }

    @Test
    @DisplayName("Somente PENDING_PAYMENT e PAID seguram estoque reservado")
    void apenasEstadosAbertosSeguramEstoque() {
        assertThat(OrderStatus.PENDING_PAYMENT.holdsStock()).isTrue();
        assertThat(OrderStatus.PAID.holdsStock()).isTrue();
        assertThat(OrderStatus.SHIPPED.holdsStock()).isFalse();
        assertThat(OrderStatus.DELIVERED.holdsStock()).isFalse();
        assertThat(OrderStatus.CANCELED.holdsStock()).isFalse();
    }
}
