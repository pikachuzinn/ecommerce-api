package dev.henan.ecommerce.order.payment;

import dev.henan.ecommerce.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PIX")
public class PixPayment extends Payment {

    @Column(name = "pix_txid", length = 40)
    private String txid;

    protected PixPayment() {
        // exigido pelo JPA
    }

    public PixPayment(String txid) {
        if (txid == null || txid.isBlank()) {
            throw new BusinessException("O txid do Pix e obrigatorio.");
        }
        this.txid = txid;
    }

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.PIX;
    }

    @Override
    public String getDescription() {
        return "Pix a vista (txid %s)".formatted(txid);
    }

    public String getTxid() {
        return txid;
    }
}
