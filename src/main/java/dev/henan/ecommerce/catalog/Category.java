package dev.henan.ecommerce.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(nullable = false, unique = true, length = 90)
    private String slug;

    protected Category() {
        // exigido pelo JPA
    }

    public Category(String name) {
        setName(name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** Manter nome e slug sincronizados evita estado inconsistente na entidade. */
    public void setName(String name) {
        this.name = name;
        this.slug = Slug.of(name);
    }

    public String getSlug() {
        return slug;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Category category && id != null && Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Category.class.hashCode();
    }
}
