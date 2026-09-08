package dev.henan.ecommerce.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base dos testes de integracao: sobe a aplicacao real contra um PostgreSQL
 * efemero em container, com o mesmo Flyway que roda em producao.
 * Testar contra o banco de verdade e o unico jeito de validar migrations,
 * constraints e o SQL gerado pelo Hibernate.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
// Cada caso roda em uma transacao que sofre rollback ao final: os testes ficam
// independentes da ordem de execucao sem precisar recriar o container.
@Transactional
public abstract class AbstractIntegrationTest {

    /**
     * Container unico para toda a JVM de teste (padrao singleton).
     *
     * Com @Testcontainers o container morreria ao fim de cada classe, mas o Spring
     * reaproveita o mesmo contexto entre as classes: a segunda classe apontaria para
     * a porta de um container ja encerrado. Iniciando manualmente, o container vive
     * enquanto a JVM viver e o Ryuk cuida de remove-lo no final.
     */
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ecommerce_test")
            .withUsername("test")
            .withPassword("test");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected MockMvc mockMvc;

    protected String adminToken;
    protected String customerToken;

    @BeforeEach
    void authenticateSeededUsers() throws Exception {
        adminToken = login("admin@ecommerce.dev", "Admin@123");
        customerToken = login("cliente@ecommerce.dev", "Admin@123");
    }

    protected String login(String email, String password) throws Exception {
        String body = """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.accessToken");
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
