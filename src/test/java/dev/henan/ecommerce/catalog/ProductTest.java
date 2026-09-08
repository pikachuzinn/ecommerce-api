package dev.henan.ecommerce.catalog;

import dev.henan.ecommerce.TestFixtures;
import dev.henan.ecommerce.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Regras de estoque e preco do produto")
class ProductTest {

    @Test
    void deveBaixarEstoqueQuandoHaSaldo() {
        Product product = TestFixtures.product(1L, "SKU-1", "100.00", 10);

        product.removeFromStock(4);

        assertThat(product.getStockQuantity()).isEqualTo(6);
    }

    @Test
    void deveRecusarBaixaMaiorQueOEstoque() {
        Product product = TestFixtures.product(1L, "SKU-1", "100.00", 3);

        assertThatThrownBy(() -> product.removeFromStock(4))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Estoque insuficiente");

        assertThat(product.getStockQuantity()).isEqualTo(3);
    }

    @Test
    void deveRecusarQuantidadeNaoPositiva() {
        Product product = TestFixtures.product(1L, "SKU-1", "100.00", 3);

        assertThatThrownBy(() -> product.removeFromStock(0)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> product.removeFromStock(-1)).isInstanceOf(BusinessException.class);
    }

    @Test
    void deveDevolverEstoqueNoCancelamento() {
        Product product = TestFixtures.product(1L, "SKU-1", "100.00", 3);

        product.removeFromStock(3);
        product.returnToStock(3);

        assertThat(product.getStockQuantity()).isEqualTo(3);
    }

    @Test
    void deveRecusarPrecoZeroOuNegativo() {
        Product product = TestFixtures.product(1L, "SKU-1", "100.00", 3);

        assertThatThrownBy(() -> product.setPrice(BigDecimal.ZERO)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> product.setPrice(new BigDecimal("-1.00"))).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Slug normaliza acentos e caracteres especiais")
    void slugDeveSerNormalizado() {
        assertThat(Slug.of("Eletronicos & Games")).isEqualTo("eletronicos-games");
        assertThat(Slug.of("Informatica")).isEqualTo("informatica");
    }
}
