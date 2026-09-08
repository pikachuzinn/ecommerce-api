package dev.henan.ecommerce.catalog;

import dev.henan.ecommerce.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String sku;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Bloqueio otimista: duas compras simultaneas do mesmo produto nao podem
     * baixar o estoque a partir da mesma leitura sem que uma delas falhe.
     */
    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Product() {
        // exigido pelo JPA
    }

    public Product(String sku, String name, String description, BigDecimal price, int stockQuantity, Category category) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        setPrice(price);
        setStockQuantity(stockQuantity);
        this.category = category;
        this.active = true;
        this.createdAt = Instant.now();
    }

    // ===== regras de dominio =====

    public boolean hasStock(int quantity) {
        return stockQuantity >= quantity;
    }

    /** Baixa o estoque ao confirmar um item de pedido. */
    public void removeFromStock(int quantity) {
        requirePositive(quantity);
        if (!hasStock(quantity)) {
            throw new BusinessException(
                    "Estoque insuficiente para o produto %s: disponivel %d, solicitado %d."
                            .formatted(sku, stockQuantity, quantity));
        }
        this.stockQuantity -= quantity;
    }

    /** Devolve o estoque quando um pedido e cancelado. */
    public void returnToStock(int quantity) {
        requirePositive(quantity);
        this.stockQuantity += quantity;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    private static void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("A quantidade deve ser maior que zero.");
        }
    }

    // ===== acessores =====

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new BusinessException("O preco deve ser maior que zero.");
        }
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new BusinessException("O estoque nao pode ser negativo.");
        }
        this.stockQuantity = stockQuantity;
    }

    public boolean isActive() {
        return active;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Product product && id != null && Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Product.class.hashCode();
    }
}
