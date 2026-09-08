package dev.henan.ecommerce.catalog;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Converte um texto livre em slug ASCII ("Eletronicos & Games" -> "eletronicos-games").
 */
public final class Slug {

    private Slug() {
    }

    public static String of(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        return normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
