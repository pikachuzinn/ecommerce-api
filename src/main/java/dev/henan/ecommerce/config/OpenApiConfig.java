package dev.henan.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearer-jwt";

    @Bean
    public OpenAPI ecommerceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-commerce Orders API")
                        .version("v1")
                        .description("""
                                API REST de catalogo e gestao de pedidos.

                                Fluxo para testar: cadastre-se em POST /api/v1/auth/register,
                                autentique em POST /api/v1/auth/login, clique em Authorize e
                                cole o accessToken retornado.
                                """)
                        .contact(new Contact().name("Henan Heiiji Shirahige").url("https://github.com/"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                .schemaRequirement(SECURITY_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Cole apenas o token, sem o prefixo Bearer."));
    }
}
