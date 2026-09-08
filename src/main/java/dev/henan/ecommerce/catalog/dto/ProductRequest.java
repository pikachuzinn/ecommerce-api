package dev.henan.ecommerce.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank(message = "O SKU e obrigatorio.")
        @Size(max = 40, message = "O SKU deve ter no maximo 40 caracteres.")
        String sku,

        @NotBlank(message = "O nome do produto e obrigatorio.")
        @Size(min = 2, max = 160, message = "O nome deve ter entre 2 e 160 caracteres.")
        String name,

        @Size(max = 2000, message = "A descricao deve ter no maximo 2000 caracteres.")
        String description,

        @NotNull(message = "O preco e obrigatorio.")
        @DecimalMin(value = "0.01", message = "O preco deve ser maior que zero.")
        @Digits(integer = 10, fraction = 2, message = "O preco aceita no maximo 2 casas decimais.")
        BigDecimal price,

        @NotNull(message = "A quantidade em estoque e obrigatoria.")
        @PositiveOrZero(message = "O estoque nao pode ser negativo.")
        Integer stockQuantity,

        @NotNull(message = "A categoria e obrigatoria.")
        Long categoryId
) {
}
