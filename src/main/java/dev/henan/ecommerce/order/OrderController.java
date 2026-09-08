package dev.henan.ecommerce.order;

import dev.henan.ecommerce.common.dto.PageResponse;
import dev.henan.ecommerce.order.dto.CreateOrderRequest;
import dev.henan.ecommerce.order.dto.OrderResponse;
import dev.henan.ecommerce.order.dto.OrderSummaryResponse;
import dev.henan.ecommerce.order.dto.PaymentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/orders")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Pedidos", description = "Fechamento, pagamento e acompanhamento de pedidos")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Fecha um novo pedido e reserva o estoque dos itens")
    public ResponseEntity<OrderResponse> create(@AuthenticationPrincipal Jwt principal,
                                                @RequestBody @Valid CreateOrderRequest request) {
        OrderResponse created = orderService.create(principal.getSubject(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/me")
    @Operation(summary = "Lista os pedidos do cliente autenticado")
    public PageResponse<OrderSummaryResponse> findMyOrders(
            @AuthenticationPrincipal Jwt principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.findMyOrders(principal.getSubject(), pageable);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todos os pedidos, opcionalmente por status (somente ADMIN)")
    public PageResponse<OrderSummaryResponse> findAll(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.findAll(status, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um pedido. Um cliente so acessa os proprios pedidos.")
    public OrderResponse findById(@AuthenticationPrincipal Jwt principal, @PathVariable Long id) {
        return orderService.findById(id, principal.getSubject());
    }

    @PostMapping("/{id}/payment")
    @Operation(summary = "Registra o pagamento e move o pedido para PAID")
    public OrderResponse pay(@AuthenticationPrincipal Jwt principal,
                             @PathVariable Long id,
                             @RequestBody @Valid PaymentRequest request) {
        return orderService.pay(id, principal.getSubject(), request);
    }

    @PostMapping("/{id}/cancellation")
    @Operation(summary = "Cancela o pedido e devolve os itens ao estoque")
    public OrderResponse cancel(@AuthenticationPrincipal Jwt principal, @PathVariable Long id) {
        return orderService.cancel(id, principal.getSubject());
    }

    @PostMapping("/{id}/shipment")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Marca o pedido como enviado (somente ADMIN)")
    public OrderResponse ship(@PathVariable Long id) {
        return orderService.ship(id);
    }

    @PostMapping("/{id}/delivery")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Marca o pedido como entregue (somente ADMIN)")
    public OrderResponse deliver(@PathVariable Long id) {
        return orderService.deliver(id);
    }
}
