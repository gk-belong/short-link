package com.gk.shortlink;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SwaggerIntegrationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testSwaggerUi_IsAccessible() {
        webTestClient.get()
            .uri("/swagger-ui.html")
            .exchange()
            .expectStatus().isFound(); // It usually redirects to /swagger-ui/index.html
    }

    @Test
    void testApiDocs_IsAccessible() {
        webTestClient.get()
            .uri("/v3/api-docs")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.openapi").exists();
    }
}
