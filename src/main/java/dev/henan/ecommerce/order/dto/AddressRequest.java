package dev.henan.ecommerce.order.dto;

import dev.henan.ecommerce.order.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(

        @NotBlank(message = "A rua e obrigatoria.")
        @Size(max = 160)
        String street,

        @NotBlank(message = "O numero e obrigatorio.")
        @Size(max = 20)
        String number,

        @Size(max = 80)
        String complement,

        @NotBlank(message = "O bairro e obrigatorio.")
        @Size(max = 80)
        String district,

        @NotBlank(message = "A cidade e obrigatoria.")
        @Size(max = 80)
        String city,

        @NotBlank(message = "A UF e obrigatoria.")
        @Pattern(regexp = "[A-Z]{2}", message = "A UF deve ter 2 letras maiusculas, ex: PR.")
        String state,

        @NotBlank(message = "O CEP e obrigatorio.")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "O CEP deve estar no formato 00000-000.")
        String zipCode
) {

    public Address toDomain() {
        return new Address(street, number, complement, district, city, state, zipCode);
    }
}
