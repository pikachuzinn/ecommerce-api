package dev.henan.ecommerce.order;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Gera codigos legiveis de pedido no formato ORD-20260903-7K2Q9M.
 * Preferimos isso a expor o id sequencial da tabela, que revela o volume de vendas.
 */
@Component
public class OrderCodeGenerator {

    private static final DateTimeFormatter DATE_PART = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sem I, O, 0, 1
    private static final int SUFFIX_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 5;

    private final SecureRandom random = new SecureRandom();
    private final OrderRepository orderRepository;

    public OrderCodeGenerator(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public String generate() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = "ORD-%s-%s".formatted(LocalDate.now().format(DATE_PART), randomSuffix());
            if (!orderRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Nao foi possivel gerar um codigo unico de pedido.");
    }

    private String randomSuffix() {
        StringBuilder suffix = new StringBuilder(SUFFIX_LENGTH);
        for (int i = 0; i < SUFFIX_LENGTH; i++) {
            suffix.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return suffix.toString();
    }
}
