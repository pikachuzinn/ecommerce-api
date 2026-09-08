package dev.henan.ecommerce.order;

import dev.henan.ecommerce.TestFixtures;
import dev.henan.ecommerce.catalog.Product;
import dev.henan.ecommerce.common.exception.BusinessException;
import dev.henan.ecommerce.order.payment.PixPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Agregado Pedido")
class OrderTest {

    private Order order;
    private Product notebook;
    private Product mouse;

    @BeforeEach
    void setUp() {
        Address address = new Address("Rua das Flores", "100", null, "Centro", "Londrina", "PR", "86010-000");
        order = new Order("ORD-TEST-1", TestFixtures.customer(1L, "cliente@teste.dev"), address, new BigDecimal("25.00"));
        notebook = TestFixtures.product(10L, "ELE-001", "1000.00", 5);
        mouse = TestFixtures.product(11L, "PER-002", "200.00", 10);
    }

    @Test
    void deveSomarItensEFreteNoTotal() {
        order.addItem(notebook, 2);
        order.addItem(mouse, 1);

        assertThat(order.getItemsTotal()).isEqualByComparingTo("2200.00");
        assertThat(order.getTotal()).isEqualByComparingTo("2225.00");
        assertThat(order.getTotalItems()).isEqualTo(3);
    }

    @Test
    @DisplayName("O mesmo produto duas vezes soma na mesma linha")
    void deveConsolidarItemRepetido() {
        order.addItem(notebook, 1);
        order.addItem(notebook, 2);

        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("O preco do item e congelado no momento da compra")
    void precoDoItemNaoAcompanhaOCatalogo() {
        order.addItem(notebook, 1);
        notebook.setPrice(new BigDecimal("1500.00"));

        assertThat(order.getItems().get(0).getUnitPrice()).isEqualByComparingTo("1000.00");
    }

    @Test
    void naoDeveAceitarQuantidadeZeroOuNegativa() {
        assertThatThrownBy(() -> order.addItem(notebook, 0)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> order.addItem(notebook, -3)).isInstanceOf(BusinessException.class);
    }

    @Test
    void deveMoverParaPagoAoRegistrarPagamento() {
        order.addItem(notebook, 1);

        order.pay(new PixPayment("txid-123"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPayment()).isNotNull();
        assertThat(order.getPayment().getOrder()).isSameAs(order);
    }

    @Test
    void naoDeveAceitarDoisPagamentos() {
        order.addItem(notebook, 1);
        order.pay(new PixPayment("txid-123"));

        assertThatThrownBy(() -> order.pay(new PixPayment("txid-456")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ja possui um pagamento");
    }

    @Test
    void naoDeveAlterarItensDepoisDePago() {
        order.addItem(notebook, 1);
        order.pay(new PixPayment("txid-123"));

        assertThatThrownBy(() -> order.addItem(mouse, 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nao pode mais ser alterado");
    }

    @Test
    void naoDeveCancelarPedidoJaEnviado() {
        order.addItem(notebook, 1);
        order.pay(new PixPayment("txid-123"));
        order.ship();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Nao e possivel mudar o pedido");
    }

    @Test
    void devePercorrerOFluxoFelizCompleto() {
        order.addItem(notebook, 1);
        order.pay(new PixPayment("txid-123"));
        order.ship();
        order.deliver();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.getStatus().isFinal()).isTrue();
    }
}
