package dev.henan.ecommerce.order;

import dev.henan.ecommerce.auth.User;
import dev.henan.ecommerce.catalog.Product;
import dev.henan.ecommerce.common.exception.BusinessException;
import dev.henan.ecommerce.order.payment.Payment;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Raiz do agregado de pedido: itens e pagamento so existem atraves dele.
 * Toda mudanca de estado passa pelos metodos desta classe, para que nenhuma
 * camada externa consiga deixar o pedido em um estado invalido.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Embedded
    private Address shippingAddress;

    @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Payment payment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected Order() {
        // exigido pelo JPA
    }

    public Order(String code, User user, Address shippingAddress, BigDecimal shippingFee) {
        this.code = code;
        this.user = user;
        this.shippingAddress = shippingAddress;
        this.shippingFee = shippingFee == null ? BigDecimal.ZERO : shippingFee;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    // ===== regras de dominio =====

    /**
     * Adiciona um produto ao pedido. Comprar o mesmo produto duas vezes soma na
     * mesma linha em vez de criar linhas duplicadas.
     */
    public void addItem(Product product, int quantity) {
        requireEditable();
        if (quantity <= 0) {
            throw new BusinessException("A quantidade do item deve ser maior que zero.");
        }

        Optional<OrderItem> existing = items.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().addQuantity(quantity);
        } else {
            items.add(new OrderItem(this, product, quantity));
        }
        touch();
    }

    /**
     * Define o frete calculado pelo servidor. So vale enquanto o pedido esta em
     * aberto: depois de pago, mudar o valor cobrado seria reescrever a historia.
     */
    public void applyShippingFee(BigDecimal fee) {
        requireEditable();
        if (fee == null || fee.signum() < 0) {
            throw new BusinessException("O frete nao pode ser negativo.");
        }
        this.shippingFee = fee;
        touch();
    }

    /** Soma dos itens, sem frete. */
    public BigDecimal getItemsTotal() {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Valor final cobrado do cliente. */
    public BigDecimal getTotal() {
        return getItemsTotal().add(shippingFee);
    }

    public int getTotalItems() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    /**
     * Registra o pagamento e move o pedido para PAID.
     * Rejeita pagamento duplicado e pagamento de pedido cancelado.
     */
    public void pay(Payment payment) {
        if (this.payment != null) {
            throw new BusinessException("O pedido %s ja possui um pagamento registrado.".formatted(code));
        }
        transitionTo(OrderStatus.PAID);
        payment.attachTo(this);
        this.payment = payment;
    }

    public void ship() {
        transitionTo(OrderStatus.SHIPPED);
    }

    public void deliver() {
        transitionTo(OrderStatus.DELIVERED);
    }

    public void cancel() {
        transitionTo(OrderStatus.CANCELED);
    }

    /**
     * Unico ponto de mudanca de status. Delegar a validacao ao enum mantem o grafo
     * de transicoes em um lugar so.
     */
    private void transitionTo(OrderStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new BusinessException(
                    "Nao e possivel mudar o pedido %s de %s para %s.".formatted(code, status, target));
        }
        this.status = target;
        touch();
    }

    private void requireEditable() {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(
                    "O pedido %s nao pode mais ser alterado no status %s.".formatted(code, status));
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public boolean belongsTo(User candidate) {
        return user != null && candidate != null && Objects.equals(user.getId(), candidate.getId());
    }

    // ===== acessores =====

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public User getUser() {
        return user;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Payment getPayment() {
        return payment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Order order && id != null && Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Order.class.hashCode();
    }
}
