package dev.henan.ecommerce.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("API de pedidos")
class OrderApiIntegrationTest extends AbstractIntegrationTest {

    private static final String NOVO_PEDIDO = """
            {
              "items": [ { "productId": 5, "quantity": 2 } ],
              "shippingAddress": {
                "street": "Rua das Flores",
                "number": "100",
                "district": "Centro",
                "city": "Londrina",
                "state": "PR",
                "zipCode": "86010-000"
              }
            }
            """;

    @Test
    @DisplayName("Fluxo completo: fechar, pagar, enviar e entregar")
    void fluxoCompletoDoPedido() throws Exception {
        String criado = mockMvc.perform(post("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, bearer(customerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVO_PEDIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        Integer orderId = JsonPath.read(criado, "$.id");

        String pagamento = """
                {"method": "PIX", "pixTxid": "txid-integracao-001"}
                """;

        mockMvc.perform(post("/api/v1/orders/{id}/payment", orderId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(customerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pagamento))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.payment.method").value("PIX"));

        // Envio e entrega sao operacoes administrativas.
        mockMvc.perform(post("/api/v1/orders/{id}/shipment", orderId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(customerToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/orders/{id}/shipment", orderId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));

        mockMvc.perform(post("/api/v1/orders/{id}/delivery", orderId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.allowedNextStates").isEmpty());

        // Pedido entregue nao volta atras.
        mockMvc.perform(post("/api/v1/orders/{id}/cancellation", orderId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(customerToken)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deveRecusarPedidoSemEstoque() throws Exception {
        String semEstoque = """
                {
                  "items": [ { "productId": 12, "quantity": 999 } ],
                  "shippingAddress": {
                    "street": "Rua das Flores", "number": "100", "district": "Centro",
                    "city": "Londrina", "state": "PR", "zipCode": "86010-000"
                  }
                }
                """;

        mockMvc.perform(post("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, bearer(customerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(semEstoque))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Estoque insuficiente")));
    }

    @Test
    @DisplayName("Requisicao sem token responde 401 no mesmo contrato de erro da API")
    void deveRecusarPedidoSemAutenticacao() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVO_PEDIDO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/orders"));
    }

    @Test
    void deveListarApenasOsPedidosDoClienteAutenticado() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, bearer(customerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVO_PEDIDO))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/orders/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // A listagem global e restrita ao admin.
        mockMvc.perform(get("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, bearer(customerToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Cancelar pedido em aberto devolve o estoque")
    void cancelamentoDevolveEstoque() throws Exception {
        String estoqueAntes = mockMvc.perform(get("/api/v1/products/5"))
                .andReturn().getResponse().getContentAsString();
        Integer antes = JsonPath.read(estoqueAntes, "$.stockQuantity");

        String criado = mockMvc.perform(post("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, bearer(customerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVO_PEDIDO))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer orderId = JsonPath.read(criado, "$.id");

        mockMvc.perform(post("/api/v1/orders/{id}/cancellation", orderId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        mockMvc.perform(get("/api/v1/products/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(antes));
    }
}
