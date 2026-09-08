package dev.henan.ecommerce.catalog.dto;

import dev.henan.ecommerce.catalog.Category;

public record CategoryResponse(Long id, String name, String slug) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug());
    }
}
