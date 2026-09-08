package dev.henan.ecommerce.order.shipping;

import dev.henan.ecommerce.order.Address;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Calcula o frete no servidor.
 *
 * O valor cobrado do cliente nunca pode vir do cliente: se o frete chegasse no
 * corpo da requisicao, qualquer um fecharia todo pedido com frete zero. A regra
 * aqui e simples de proposito (faixa por regiao + frete gratis acima de um piso);
 * o ponto e que ela mora do lado de ca.
 */
@Component
public class ShippingCalculator {

    /** Acima deste subtotal o frete e por conta da loja. */
    static final BigDecimal FREE_SHIPPING_FROM = new BigDecimal("299.00");

    private static final Set<String> SUL_SUDESTE = Set.of("SP", "RJ", "MG", "ES", "PR", "SC", "RS");
    private static final Set<String> CENTRO_OESTE = Set.of("GO", "MT", "MS", "DF");
    private static final Set<String> NORDESTE = Set.of("BA", "SE", "AL", "PE", "PB", "RN", "CE", "PI", "MA");

    private static final BigDecimal FEE_SUL_SUDESTE = new BigDecimal("19.90");
    private static final BigDecimal FEE_CENTRO_OESTE = new BigDecimal("29.90");
    private static final BigDecimal FEE_NORDESTE = new BigDecimal("34.90");
    private static final BigDecimal FEE_NORTE = new BigDecimal("49.90");

    /**
     * @param destination endereco de entrega do pedido
     * @param itemsTotal  subtotal dos itens, sem frete
     * @return o frete a cobrar, nunca negativo
     */
    public BigDecimal calculate(Address destination, BigDecimal itemsTotal) {
        if (itemsTotal != null && itemsTotal.compareTo(FREE_SHIPPING_FROM) >= 0) {
            return BigDecimal.ZERO;
        }
        return feeFor(destination == null ? null : destination.getState());
    }

    /** UF desconhecida cai na faixa mais cara: e melhor cobrar a mais do que fechar no prejuizo. */
    private BigDecimal feeFor(String state) {
        if (state == null) {
            return FEE_NORTE;
        }
        String uf = state.trim().toUpperCase();

        if (SUL_SUDESTE.contains(uf)) {
            return FEE_SUL_SUDESTE;
        }
        if (CENTRO_OESTE.contains(uf)) {
            return FEE_CENTRO_OESTE;
        }
        if (NORDESTE.contains(uf)) {
            return FEE_NORDESTE;
        }
        return FEE_NORTE;
    }
}
