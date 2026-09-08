package dev.henan.ecommerce.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("API de autenticacao")
class AuthApiIntegrationTest extends AbstractIntegrationTest {

    @Test
    void deveCadastrarNovoClienteComPapelPadrao() throws Exception {
        String body = """
                {"name": "Maria Souza", "email": "maria.souza@teste.dev", "password": "SenhaForte1"}
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("maria.souza@teste.dev"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_CUSTOMER"));
    }

    @Test
    void deveRecusarEmailDuplicado() throws Exception {
        String body = """
                {"name": "Outro Admin", "email": "admin@ecommerce.dev", "password": "SenhaForte1"}
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Ja existe uma conta cadastrada com este e-mail."));
    }

    @Test
    void deveRecusarSenhaCurta() throws Exception {
        String body = """
                {"name": "Joao", "email": "joao@teste.dev", "password": "123"}
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields[0].field").value("password"));
    }

    @Test
    void deveRecusarCredenciaisInvalidas() throws Exception {
        String body = """
                {"email": "admin@ecommerce.dev", "password": "senha-errada"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
