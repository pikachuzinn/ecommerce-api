package dev.henan.ecommerce.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Carrega o pedido com itens, produtos e usuario em uma consulta so.
     * Sem isso, montar o OrderResponse dispararia um select por item (N+1).
     */
    @EntityGraph(attributePaths = {"user", "items", "items.product", "payment"})
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"user", "items", "items.product", "payment"})
    Optional<Order> findByCode(String code);

    @EntityGraph(attributePaths = {"items"})
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"items"})
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    boolean existsByCode(String code);
}
