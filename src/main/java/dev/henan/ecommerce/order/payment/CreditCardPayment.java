package dev.henan.ecommerce.order.payment;

import dev.henan.ecommerce.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CREDIT_CARD")
public class CreditCardPayment extends Payment {

    private static final int MAX_INSTALLMENTS = 12;

    @Column(name = "card_brand", length = 30)
    private String cardBrand;

    @Column(name = "card_last4", length = 4)
    private String cardLast4;

    @Column(name = "installments")
    private Integer installments;

    protected CreditCardPayment() {
        // exigido pelo JPA
    }

    public CreditCardPayment(String cardBrand, String cardLast4, int installments) {
        if (installments < 1 || installments > MAX_INSTALLMENTS) {
            throw new BusinessException("O parcelamento deve ser de 1 a %d vezes.".formatted(MAX_INSTALLMENTS));
        }
        if (cardLast4 == null || !cardLast4.matches("\\d{4}")) {
            throw new BusinessException("Informe exatamente os 4 ultimos digitos do cartao.");
        }
        this.cardBrand = cardBrand;
        this.cardLast4 = cardLast4;
        this.installments = installments;
    }

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.CREDIT_CARD;
    }

    @Override
    public String getDescription() {
        return "%s **** %s em %dx".formatted(cardBrand, cardLast4, installments);
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public String getCardLast4() {
        return cardLast4;
    }

    public Integer getInstallments() {
        return installments;
    }
}
