package dev.henan.ecommerce.order;

import dev.henan.ecommerce.TestFixtures;
import dev.henan.ecommerce.auth.User;
import dev.henan.ecommerce.auth.UserRepository;
import dev.henan.ecommerce.catalog.Product;
import dev.henan.ecommerce.catalog.ProductRepository;
import dev.henan.ecommerce.common.exception.BusinessException;
import dev.henan.ecommerce.common.exception.ResourceNotFoundException;
import dev.henan.ecommerce.order.dto.AddressRequest;
import dev.henan.ecommerce.order.dto.CreateOrderRequest;
import dev.henan.ecommerce.order.dto.OrderItemRequest;
import dev.henan.ecommerce.order.dto.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderCodeGenerator codeGenerator;

    @InjectMocks
    private OrderService orderService;

    private User customer;
    private Product notebook;

    private static final String CUSTOMER_EMAIL = "cliente@teste.dev";

    @BeforeEach
    void setUp() {
        customer = TestFixtures.customer(1L, CUSTOMER_EMAIL);
        notebook = TestFixtures.product(10L, "ELE-001", "1000.00", 5);
        lenient().when(userRepository.findByEmail(CUSTOMER_EMAIL)).thenReturn(Optional.of(customer));
        lenient().when(codeGenerator.generate()).thenReturn("ORD-20260903-ABC123");
        lenient().when(orderRepository.save(any(Order.class))).thenAnswer(call -> call.getArgument(0));
    }

    private CreateOrderRequest request(OrderItemRequest... items) {
        AddressRequest address = new AddressRequest(
                "Rua das Flores", "100", null, "Centro", "Londrina", "PR", "86010-000");
        return new CreateOrderRequest(List.of(items), address, new BigDecimal("25.00"));
    }

    @Test
    @DisplayName("Fechar pedido baixa o estoque e calcula o total")
    void deveCriarPedidoEBaixarEstoque() {
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(notebook));

        OrderResponse response = orderService.create(CUSTOMER_EMAIL, request(new OrderItemRequest(10L, 2)));

        assertThat(notebook.getStockQuantity()).isEqualTo(3);
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(response.total()).isEqualByComparingTo("2025.00");
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Linhas repetidas do mesmo produto sao somadas antes de validar o estoque")
    void deveConsolidarQuantidadesAntesDeValidarEstoque() {
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(notebook));

        assertThatThrownBy(() -> orderService.create(CUSTOMER_EMAIL,
                request(new OrderItemRequest(10L, 3), new OrderItemRequest(10L, 3))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Estoque insuficiente");

        assertThat(notebook.getStockQuantity()).isEqualTo(5);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void deveRecusarPedidoComProdutoInativo() {
        notebook.deactivate();
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(notebook));

        assertThatThrownBy(() -> orderService.create(CUSTOMER_EMAIL, request(new OrderItemRequest(10L, 1))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nao esta disponivel");
    }

    @Test
    void deveRecusarPedidoComProdutoInexistente() {
        when(productRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(CUSTOMER_EMAIL, request(new OrderItemRequest(99L, 1))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Cancelar pedido em aberto devolve os itens ao estoque")
    void deveDevolverEstoqueAoCancelar() {
        Order order = pendingOrderWith(notebook, 2);
        notebook.removeFromStock(2);
        when(orderRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(order));
        when(productRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(notebook));

        OrderResponse response = orderService.cancel(5L, CUSTOMER_EMAIL);

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELED);
        assertThat(notebook.getStockQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Um cliente nao acessa o pedido de outro")
    void deveNegarAcessoAPedidoDeOutroCliente() {
        Order order = pendingOrderWith(notebook, 1);
        when(orderRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(order));

        User intruso = TestFixtures.customer(2L, "intruso@teste.dev");
        when(userRepository.findByEmail("intruso@teste.dev")).thenReturn(Optional.of(intruso));

        assertThatThrownBy(() -> orderService.findById(5L, "intruso@teste.dev"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Um admin acessa o pedido de qualquer cliente")
    void adminDeveAcessarQualquerPedido() {
        Order order = pendingOrderWith(notebook, 1);
        when(orderRepository.findByIdWithDetails(5L)).thenReturn(Optional.of(order));

        User admin = TestFixtures.admin(3L, "admin@teste.dev");
        when(userRepository.findByEmail("admin@teste.dev")).thenReturn(Optional.of(admin));

        assertThat(orderService.findById(5L, "admin@teste.dev").code()).isEqualTo("ORD-TEST");
    }

    private Order pendingOrderWith(Product product, int quantity) {
        Address address = new Address("Rua das Flores", "100", null, "Centro", "Londrina", "PR", "86010-000");
        Order order = new Order("ORD-TEST", customer, address, BigDecimal.ZERO);
        order.addItem(product, quantity);
        return order;
    }
}
