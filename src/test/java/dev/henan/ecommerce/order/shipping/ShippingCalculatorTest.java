package dev.henan.ecommerce.order.shipping;

import dev.henan.ecommerce.order.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ShippingCalculator")
class ShippingCalculatorTest {

    private final ShippingCalculator calculator = new ShippingCalculator();

    private Address addressIn(String state) {
        return new Address("Rua das Flores", "100", null, "Centro", "Cidade", state, "86010-000");
    }

    @Test
    @DisplayName("Acima do piso o frete e por conta da loja")
    void deveZerarFreteAcimaDoPiso() {
        assertThat(calculator.calculate(addressIn("AM"), new BigDecimal("299.00")))
                .isEqualByComparingTo("0.00");
        assertThat(calculator.calculate(addressIn("AM"), new BigDecimal("1000.00")))
                .isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Abaixo do piso o frete varia por regiao")
    void deveCobrarPorRegiao() {
        BigDecimal subtotal = new BigDecimal("100.00");

        assertThat(calculator.calculate(addressIn("PR"), subtotal)).isEqualByComparingTo("19.90");
        assertThat(calculator.calculate(addressIn("DF"), subtotal)).isEqualByComparingTo("29.90");
        assertThat(calculator.calculate(addressIn("BA"), subtotal)).isEqualByComparingTo("34.90");
        assertThat(calculator.calculate(addressIn("AM"), subtotal)).isEqualByComparingTo("49.90");
    }

    @Test
    @DisplayName("UF em minusculas ou com espaco continua sendo a mesma UF")
    void deveNormalizarAUf() {
        assertThat(calculator.calculate(addressIn(" pr "), new BigDecimal("100.00")))
                .isEqualByComparingTo("19.90");
    }

    @Test
    @DisplayName("UF desconhecida cai na faixa mais cara, nunca em frete zero")
    void deveUsarAFaixaMaisCaraQuandoNaoSabeAUf() {
        assertThat(calculator.calculate(addressIn("XX"), new BigDecimal("100.00")))
                .isEqualByComparingTo("49.90");
        assertThat(calculator.calculate(addressIn(null), new BigDecimal("100.00")))
                .isEqualByComparingTo("49.90");
        assertThat(calculator.calculate(null, new BigDecimal("100.00")))
                .isEqualByComparingTo("49.90");
    }
}
