package dev.henan.ecommerce.catalog;

import dev.henan.ecommerce.catalog.dto.CategoryRequest;
import dev.henan.ecommerce.catalog.dto.CategoryResponse;
import dev.henan.ecommerce.common.exception.BusinessException;
import dev.henan.ecommerce.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll(Sort.by("name")).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return CategoryResponse.from(getEntity(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String slug = Slug.of(request.name());
        if (categoryRepository.existsBySlug(slug)) {
            throw new BusinessException("Ja existe uma categoria equivalente a '%s'.".formatted(request.name()));
        }
        return CategoryResponse.from(categoryRepository.save(new Category(request.name().trim())));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getEntity(id);
        String slug = Slug.of(request.name());

        categoryRepository.findBySlug(slug)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("Ja existe outra categoria equivalente a '%s'.".formatted(request.name()));
                });

        category.setName(request.name().trim());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = getEntity(id);
        categoryRepository.delete(category);
    }

    /** Uso interno de outros services: devolve a entidade gerenciada, nao o DTO. */
    @Transactional(readOnly = true)
    public Category getEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Categoria", id));
    }
}
