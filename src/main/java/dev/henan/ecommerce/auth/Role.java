package dev.henan.ecommerce.auth;

/**
 * Papeis de acesso. O prefixo ROLE_ e a convencao esperada por hasRole() do Spring Security.
 */
public enum Role {

    ROLE_CUSTOMER,
    ROLE_ADMIN;

    /** Nome sem o prefixo, usado em hasRole(...) e nas claims do token. */
    public String shortName() {
        return name().substring("ROLE_".length());
    }
}
