package dev.henan.ecommerce.order.payment;

import dev.henan.ecommerce.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
@DiscriminatorValue("BOLETO")
public class BoletoPayment extends Payment {

    @Column(name = "boleto_barcode", length = 60)
    private String barcode;

    @Column(name = "boleto_due_date")
    private LocalDate dueDate;

    protected BoletoPayment() {
        // exigido pelo JPA
    }

    public BoletoPayment(String barcode, LocalDate dueDate) {
        if (barcode == null || barcode.isBlank()) {
            throw new BusinessException("A linha digitavel do boleto e obrigatoria.");
        }
        if (dueDate == null || dueDate.isBefore(LocalDate.now())) {
            throw new BusinessException("O vencimento do boleto deve ser hoje ou uma data futura.");
        }
        this.barcode = barcode;
        this.dueDate = dueDate;
    }

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.BOLETO;
    }

    @Override
    public String getDescription() {
        return "Boleto com vencimento em %s".formatted(dueDate);
    }

    public String getBarcode() {
        return barcode;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }
}
