package dev.henan.ecommerce.catalog;

import dev.henan.ecommerce.catalog.dto.ProductRequest;
import dev.henan.ecommerce.catalog.dto.ProductResponse;
import dev.henan.ecommerce.common.dto.PageResponse;
import dev.henan.ecommerce.common.exception.BusinessException;
import dev.henan.ecommerce.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String name,
                                                Long categoryId,
                                                BigDecimal minPrice,
                                                BigDecimal maxPrice,
                                                boolean activeOnly,
                                                Pageable pageable) {

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BusinessException("O preco minimo nao pode ser maior que o preco maximo.");
        }

        List<Specification<Product>> filters = new ArrayList<>();
        filters.add(ProductSpecifications.nameContains(name));
        filters.add(ProductSpecifications.inCategory(categoryId));
        filters.add(ProductSpecifications.priceAtLeast(minPrice));
        filters.add(ProductSpecifications.priceAtMost(maxPrice));
        filters.add(ProductSpecifications.onlyActive(activeOnly));

        Specification<Product> spec = ProductSpecifications.fetchCategory();
        for (Specification<Product> filter : filters) {
            if (filter != null) {
                spec = spec.and(filter);
            }
        }

        Page<Product> page = productRepository.findAll(spec, pageable);
        return PageResponse.from(page, ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return ProductResponse.from(getEntity(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        String sku = request.sku().trim().toUpperCase();
        if (productRepository.existsBySku(sku)) {
            throw new BusinessException("Ja existe um produto com o SKU %s.".formatted(sku));
        }

        Category category = categoryService.getEntity(request.categoryId());

        Product product = new Product(
                sku,
                request.name().trim(),
                request.description(),
                request.price(),
                request.stockQuantity(),
                category);

        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getEntity(id);
        String sku = request.sku().trim().toUpperCase();

        if (!product.getSku().equals(sku) && productRepository.existsBySku(sku)) {
            throw new BusinessException("Ja existe outro produto com o SKU %s.".formatted(sku));
        }

        product.setSku(sku);
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setCategory(categoryService.getEntity(request.categoryId()));

        return ProductResponse.from(product);
    }

    /**
     * Desativa em vez de apagar: produtos ja referenciados por pedidos historicos
     * precisam continuar existindo.
     */
    @Transactional
    public void deactivate(Long id) {
        getEntity(id).deactivate();
    }

    @Transactional(readOnly = true)
    public Product getEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Produto", id));
    }
}
