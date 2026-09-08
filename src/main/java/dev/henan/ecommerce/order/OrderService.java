package dev.henan.ecommerce.order;

import dev.henan.ecommerce.auth.User;
import dev.henan.ecommerce.auth.UserRepository;
import dev.henan.ecommerce.catalog.Product;
import dev.henan.ecommerce.catalog.ProductRepository;
import dev.henan.ecommerce.common.dto.PageResponse;
import dev.henan.ecommerce.common.exception.BusinessException;
import dev.henan.ecommerce.common.exception.ResourceNotFoundException;
import dev.henan.ecommerce.order.dto.CreateOrderRequest;
import dev.henan.ecommerce.order.dto.OrderItemRequest;
import dev.henan.ecommerce.order.dto.OrderResponse;
import dev.henan.ecommerce.order.dto.OrderSummaryResponse;
import dev.henan.ecommerce.order.dto.PaymentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderCodeGenerator codeGenerator;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository,
                        OrderCodeGenerator codeGenerator) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.codeGenerator = codeGenerator;
    }

    /**
     * Fecha um pedido: valida os produtos, reserva o estoque e persiste tudo em
     * uma unica transacao. Se qualquer item falhar, nada e gravado e o estoque
     * dos itens anteriores nao fica reservado.
     */
    @Transactional
    public OrderResponse create(String userEmail, CreateOrderRequest request) {
        User user = requireUser(userEmail);

        // Consolida quantidades por produto antes de tocar no estoque: se o cliente
        // mandar o mesmo produto em duas linhas, a validacao precisa ver o total.
        Map<Long, Integer> quantityByProduct = new LinkedHashMap<>();
        for (OrderItemRequest item : request.items()) {
            quantityByProduct.merge(item.productId(), item.quantity(), Integer::sum);
        }

        BigDecimal shippingFee = request.shippingFee() == null ? BigDecimal.ZERO : request.shippingFee();
        Order order = new Order(codeGenerator.generate(), user, request.shippingAddress().toDomain(), shippingFee);

        for (Map.Entry<Long, Integer> entry : quantityByProduct.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();

            // Lock pessimista: serializa duas compras concorrentes do mesmo produto.
            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Produto", productId));

            if (!product.isActive()) {
                throw new BusinessException("O produto %s nao esta disponivel para venda.".formatted(product.getSku()));
            }

            product.removeFromStock(quantity);
            order.addItem(product, quantity);
        }

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id, String userEmail) {
        Order order = getEntityWithDetails(id);
        requireOwnerOrAdmin(order, requireUser(userEmail));
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> findMyOrders(String userEmail, Pageable pageable) {
        User user = requireUser(userEmail);
        Page<Order> page = orderRepository.findByUserId(user.getId(), pageable);
        return PageResponse.from(page, OrderSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> findAll(OrderStatus status, Pageable pageable) {
        Page<Order> page = status == null
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);
        return PageResponse.from(page, OrderSummaryResponse::from);
    }

    @Transactional
    public OrderResponse pay(Long id, String userEmail, PaymentRequest request) {
        Order order = getEntityWithDetails(id);
        requireOwnerOrAdmin(order, requireUser(userEmail));
        order.pay(request.toDomain());
        return OrderResponse.from(order);
    }

    /**
     * Cancela o pedido e devolve ao estoque tudo que havia sido reservado.
     * O que decide se ha estoque a devolver e o status anterior, nao o novo.
     */
    @Transactional
    public OrderResponse cancel(Long id, String userEmail) {
        Order order = getEntityWithDetails(id);
        requireOwnerOrAdmin(order, requireUser(userEmail));

        boolean shouldRestoreStock = order.getStatus().holdsStock();
        order.cancel();

        if (shouldRestoreStock) {
            for (OrderItem item : order.getItems()) {
                productRepository.findByIdForUpdate(item.getProduct().getId())
                        .ifPresent(product -> product.returnToStock(item.getQuantity()));
            }
        }

        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse ship(Long id) {
        Order order = getEntityWithDetails(id);
        order.ship();
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse deliver(Long id) {
        Order order = getEntityWithDetails(id);
        order.deliver();
        return OrderResponse.from(order);
    }

    private Order getEntityWithDetails(Long id) {
        return orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Pedido", id));
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado nao encontrado: " + email));
    }

    /** Um cliente so enxerga os proprios pedidos; um admin enxerga todos. */
    private void requireOwnerOrAdmin(Order order, User requester) {
        if (!requester.isAdmin() && !order.belongsTo(requester)) {
            throw new AccessDeniedException("Este pedido pertence a outro cliente.");
        }
    }
}
