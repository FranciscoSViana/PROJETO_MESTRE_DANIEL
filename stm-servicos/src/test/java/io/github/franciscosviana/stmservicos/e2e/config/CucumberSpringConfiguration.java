package io.github.franciscosviana.stmservicos.e2e.config;

import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.annotation.PostConstruct;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@Testcontainers
public class CucumberSpringConfiguration {

    @LocalServerPort
    private int port;

    // ── Testcontainers ──────────────────────────────────────────────────────

    @DynamicPropertySource
    static void configureContainers(DynamicPropertyRegistry registry) {
        PostgresContainerConfig.configurePostgres(registry);
        WireMockConfig.configureWireMock(registry);
    }

    @PostConstruct
    void configurarRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
