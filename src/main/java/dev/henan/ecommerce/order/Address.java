package dev.henan.ecommerce.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Endereco de entrega gravado como snapshot no pedido.
 * Se o cliente mudar de endereco depois, o pedido antigo continua contando a verdade
 * de para onde a mercadoria foi enviada.
 */
@Embeddable
public class Address {

    @Column(name = "shipping_street", length = 160)
    private String street;

    @Column(name = "shipping_number", length = 20)
    private String number;

    @Column(name = "shipping_complement", length = 80)
    private String complement;

    @Column(name = "shipping_district", length = 80)
    private String district;

    @Column(name = "shipping_city", length = 80)
    private String city;

    @Column(name = "shipping_state", length = 2)
    private String state;

    @Column(name = "shipping_zip_code", length = 9)
    private String zipCode;

    protected Address() {
        // exigido pelo JPA
    }

    public Address(String street, String number, String complement, String district,
                   String city, String state, String zipCode) {
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.district = district;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
    }

    public String getStreet() {
        return street;
    }

    public String getNumber() {
        return number;
    }

    public String getComplement() {
        return complement;
    }

    public String getDistrict() {
        return district;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZipCode() {
        return zipCode;
    }
}
