package dev.henan.ecommerce.order;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Ciclo de vida do pedido. As transicoes permitidas ficam declaradas aqui, e nao
 * espalhadas em ifs pelo service — assim a regra tem um unico lugar para mudar.
 *
 * <pre>
 * PENDING_PAYMENT -> PAID -> SHIPPED -> DELIVERED
 *        |            |
 *        +------------+--> CANCELED
 * </pre>
 */
public enum OrderStatus {

    PENDING_PAYMENT,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<OrderStatus, Set<OrderStatus>> transitions = new EnumMap<>(OrderStatus.class);
        transitions.put(PENDING_PAYMENT, EnumSet.of(PAID, CANCELED));
        transitions.put(PAID, EnumSet.of(SHIPPED, CANCELED));
        transitions.put(SHIPPED, EnumSet.of(DELIVERED));
        transitions.put(DELIVERED, EnumSet.noneOf(OrderStatus.class));
        transitions.put(CANCELED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS = Collections.unmodifiableMap(transitions);
    }

    public boolean canTransitionTo(OrderStatus target) {
        return ALLOWED_TRANSITIONS.get(this).contains(target);
    }

    public Set<OrderStatus> nextStates() {
        return ALLOWED_TRANSITIONS.get(this);
    }

    /** Estados em que o estoque ja foi reservado e precisa voltar no cancelamento. */
    public boolean holdsStock() {
        return this == PENDING_PAYMENT || this == PAID;
    }

    public boolean isFinal() {
        return ALLOWED_TRANSITIONS.get(this).isEmpty();
    }
}
