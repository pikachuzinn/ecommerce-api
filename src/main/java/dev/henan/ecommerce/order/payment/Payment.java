package dev.henan.ecommerce.order.payment;

import dev.henan.ecommerce.order.Order;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

/**
 * Pagamento de um pedido. Modelado com heranca porque cada meio de pagamento
 * carrega dados proprios (bandeira e parcelas no cartao, txid no Pix, linha
 * digitavel no boleto) mas todos respondem ao mesmo contrato.
 *
 * Estrategia SINGLE_TABLE: uma tabela so, discriminada por payment_type. Troca
 * espaco (colunas nulas para os tipos que nao as usam) por consultas polimorficas
 * sem join, que e o padrao de acesso deste dominio.
 */
@Entity
@Table(name = "payments")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "payment_type", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "paid_at", nullable = false)
    private Instant paidAt = Instant.now();

    protected Payment() {
        // exigido pelo JPA
    }

    /** Meio de pagamento concreto — implementado por cada subclasse. */
    public abstract PaymentMethod getMethod();

    /**
     * Texto curto para exibir ao cliente ("Visa **** 4242 em 3x").
     * Cada subclasse decide como se descreve: polimorfismo em vez de switch por tipo.
     */
    public abstract String getDescription();

    /** Ligacao bidirecional feita pelo agregado Order. */
    public void attachTo(Order order) {
        this.order = order;
        this.paidAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Payment payment && id != null && Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return Payment.class.hashCode();
    }
}
