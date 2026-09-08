package dev.henan.ecommerce;

import dev.henan.ecommerce.auth.Role;
import dev.henan.ecommerce.auth.User;
import dev.henan.ecommerce.catalog.Category;
import dev.henan.ecommerce.catalog.Product;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Fabricas de objetos de dominio para os testes.
 * O id e injetado por reflexao porque em producao quem o atribui e o banco.
 */
public final class TestFixtures {

    private TestFixtures() {
    }

    public static User customer(long id, String email) {
        User user = new User("Cliente " + id, email, "{bcrypt}hash", Set.of(Role.ROLE_CUSTOMER));
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    public static User admin(long id, String email) {
        User user = new User("Admin " + id, email, "{bcrypt}hash", Set.of(Role.ROLE_ADMIN, Role.ROLE_CUSTOMER));
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    public static Category category(long id, String name) {
        Category category = new Category(name);
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    public static Product product(long id, String sku, String price, int stock) {
        Product product = new Product(sku, "Produto " + sku, "descricao",
                new BigDecimal(price), stock, category(1L, "Eletronicos"));
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
