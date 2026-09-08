package dev.henan.ecommerce.order.dto;

import dev.henan.ecommerce.common.exception.BusinessException;
import dev.henan.ecommerce.order.payment.BoletoPayment;
import dev.henan.ecommerce.order.payment.CreditCardPayment;
import dev.henan.ecommerce.order.payment.Payment;
import dev.henan.ecommerce.order.payment.PaymentMethod;
import dev.henan.ecommerce.order.payment.PixPayment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Entrada unica para os tres meios de pagamento. Os campos especificos de cada
 * meio sao opcionais no contrato e obrigatorios na fabrica abaixo, que e quem
 * decide qual subclasse de Payment instanciar.
 */
public record PaymentRequest(

        @NotNull(message = "O meio de pagamento e obrigatorio.")
        PaymentMethod method,

        @Size(max = 30) String cardBrand,
        @Size(min = 4, max = 4) String cardLast4,
        Integer installments,

        @Size(max = 40) String pixTxid,

        @Size(max = 60) String boletoBarcode,
        LocalDate boletoDueDate
) {

    /** Fabrica polimorfica: converte o DTO plano na subclasse concreta de Payment. */
    public Payment toDomain() {
        return switch (method) {
            case CREDIT_CARD -> {
                requirePresent(cardBrand, "cardBrand");
                requirePresent(cardLast4, "cardLast4");
                if (installments == null) {
                    throw new BusinessException("O campo 'installments' e obrigatorio para pagamento em cartao.");
                }
                yield new CreditCardPayment(cardBrand, cardLast4, installments);
            }
            case PIX -> {
                requirePresent(pixTxid, "pixTxid");
                yield new PixPayment(pixTxid);
            }
            case BOLETO -> {
                requirePresent(boletoBarcode, "boletoBarcode");
                if (boletoDueDate == null) {
                    throw new BusinessException("O campo 'boletoDueDate' e obrigatorio para pagamento em boleto.");
                }
                yield new BoletoPayment(boletoBarcode, boletoDueDate);
            }
        };
    }

    private void requirePresent(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(
                    "O campo '%s' e obrigatorio para pagamento via %s.".formatted(field, method));
        }
    }
}
