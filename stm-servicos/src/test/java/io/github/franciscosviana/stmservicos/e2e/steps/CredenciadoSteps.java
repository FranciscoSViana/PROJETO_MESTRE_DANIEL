package io.github.franciscosviana.stmservicos.e2e.steps;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.github.franciscosviana.stmservicos.e2e.config.ScenarioContext;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class CredenciadoSteps {

    private final ScenarioContext ctx;

    @Dado("que existe {int} credenciado cadastrado")
    public void existeCredenciadoCadastrado(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            Response resp = criarCredenciado("CREDENCIADO " + i + " LTDA", gerarCnpj(i));
            assertThat(resp.statusCode()).isEqualTo(200);
            ctx.setCredenciadoId(UUID.fromString(resp.jsonPath().getString("id")));
        }
    }

    @Quando("eu faço POST em {string} com dados de credenciado válidos")
    public void postCredenciado(String path) {
        ctx.setLastResponse(criarCredenciado("NOVO CREDENCIADO LTDA", "77.888.999/0001-55"));
    }

    @Quando("eu faço GET em {string} para credenciado")
    public void getCredenciado(String path) {
        String pathResolvido = path.replace("{id}",
                ctx.getCredenciadoId() != null ? ctx.getCredenciadoId().toString() : UUID.randomUUID().toString());
        ctx.setLastResponse(
                given()
                        .header("Authorization", "Bearer " + ctx.getAuthToken())
                        .when()
                        .get(pathResolvido)
        );
    }

    private Response criarCredenciado(String rag, String cnpj) {
        return given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "rag", rag,
                        "tipoPessoa", "PESSOA_JURIDICA",
                        "numeroPessoa", cnpj,
                        "valorChamado", 120.00,
                        "valorKm", 2.00,
                        "tipoFluxoPagamento", "POR_OS",
                        "contato", "Contato X",
                        "email", "credenciado@email.com",
                        "endereco", Map.of(
                                "cep", "64000-000", "logradouro", "Rua Técnica",
                                "numero", "50", "bairro", "Norte",
                                "cidade", "Teresina", "estado", "PI"
                        )
                ))
                .when()
                .post("/api/credenciados");
    }

    private String gerarCnpj(int index) {
        return String.format("%02d.222.333/0001-55", index + 10);
    }
}
