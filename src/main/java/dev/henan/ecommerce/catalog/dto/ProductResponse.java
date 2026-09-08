package dev.henan.ecommerce.catalog.dto;

import dev.henan.ecommerce.catalog.Product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        int stockQuantity,
        boolean active,
        CategoryResponse category
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.isActive(),
                CategoryResponse.from(product.getCategory()));
    }
}
