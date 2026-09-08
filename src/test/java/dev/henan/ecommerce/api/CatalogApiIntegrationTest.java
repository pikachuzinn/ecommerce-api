package dev.henan.ecommerce.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("API de catalogo")
class CatalogApiIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("A vitrine e publica e vem paginada")
    void listagemDeProdutosEPublica() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(12));
    }

    @Test
    void deveFiltrarProdutosPorNomeEFaixaDePreco() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("name", "monitor")
                        .param("minPrice", "1000")
                        .param("maxPrice", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].sku").value("ELE-002"));
    }

    @Test
    void deveRecusarFaixaDePrecoInvertida() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("minPrice", "500")
                        .param("maxPrice", "100"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void deveRetornar404ParaProdutoInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/products/999999"));
    }

    @Test
    @DisplayName("Cadastro de produto exige token")
    void escritaSemTokenDeveRetornar401() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(novoProdutoJson("NEW-001")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cliente autenticado nao pode cadastrar produto")
    void escritaComTokenDeClienteDeveRetornar403() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, bearer(customerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(novoProdutoJson("NEW-002")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDeveCadastrarProduto() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(novoProdutoJson("NEW-003")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("NEW-003"))
                .andExpect(jsonPath("$.category.name").value("Eletronicos"));
    }

    @Test
    void deveRejeitarProdutoComCamposInvalidos() throws Exception {
        String invalido = """
                {"sku": "", "name": "x", "price": -5, "stockQuantity": -1, "categoryId": null}
                """;

        mockMvc.perform(post("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields").isNotEmpty());
    }

    private String novoProdutoJson(String sku) {
        return """
                {
                  "sku": "%s",
                  "name": "Produto de teste",
                  "description": "Criado pelo teste de integracao",
                  "price": 199.90,
                  "stockQuantity": 10,
                  "categoryId": 1
                }
                """.formatted(sku);
    }
}
