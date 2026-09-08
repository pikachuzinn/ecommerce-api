package dev.henan.ecommerce.catalog;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Predicados reutilizaveis para a busca de produtos.
 * Cada filtro devolve null quando o parametro nao foi informado; o Spring Data
 * ignora predicados nulos ao combinar as Specifications.
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> nameContains(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String pattern = "%" + name.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    public static Specification<Product> inCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> priceAtLeast(BigDecimal minPrice) {
        if (minPrice == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> priceAtMost(BigDecimal maxPrice) {
        if (maxPrice == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> onlyActive(boolean activeOnly) {
        if (!activeOnly) {
            return null;
        }
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    /**
     * Carrega a categoria junto do produto para evitar N+1 na montagem da resposta.
     * O fetch e aplicado apenas na consulta de dados, nunca na de contagem.
     */
    public static Specification<Product> fetchCategory() {
        return (root, query, cb) -> {
            if (query != null && Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("category", JoinType.INNER);
            }
            return cb.conjunction();
        };
    }
}
