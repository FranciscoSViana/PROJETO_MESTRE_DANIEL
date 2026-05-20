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
public class ClienteSteps {

    private final ScenarioContext ctx;

    @Dado("que existe um cliente cadastrado")
    public void existeClienteCadastrado() {
        Response resp = criarCliente("EMPRESA TESTE LTDA", "12.345.678/0001-90");
        assertThat(resp.statusCode()).isEqualTo(200);
        ctx.setClienteId(UUID.fromString(resp.jsonPath().getString("id")));
        ctx.setLastCreatedId(ctx.getClienteId());
    }

    @Dado("que existem {int} clientes cadastrados")
    public void existemClientesCadastrados(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            Response resp = criarCliente("EMPRESA " + i + " LTDA", gerarCnpj(i));
            assertThat(resp.statusCode()).isEqualTo(200);
        }
    }

    @Quando("eu faço POST em {string} com dados de cliente válidos")
    public void postCliente(String path) {
        ctx.setLastResponse(criarCliente("NOVA EMPRESA LTDA", "98.765.432/0001-10"));
    }

    @Quando("eu faço PUT em {string} com dados atualizados")
    public void putCliente(String path) {
        String pathResolvido = resolverPath(path);
        ctx.setLastResponse(
                given()
                        .header("Authorization", "Bearer " + ctx.getAuthToken())
                        .contentType(ContentType.JSON)
                        .body(Map.of(
                                "nome", "EMPRESA ATUALIZADA LTDA",
                                "cnpj", "12.345.678/0001-90",
                                "razaoSocial", "EMPRESA ATUALIZADA LTDA",
                                "valorChamado", 200.00,
                                "valorKm", 3.00,
                                "tipoFluxoPagamento", "MENSAL",
                                "endereco", Map.of(
                                        "cep", "64000-000", "logradouro", "Av. Atualizada",
                                        "numero", "200", "bairro", "Centro",
                                        "cidade", "Teresina", "estado", "PI"
                                )
                        ))
                        .when()
                        .put(pathResolvido)
        );
    }

    private Response criarCliente(String nome, String cnpj) {
        return given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "nome", nome,
                        "cnpj", cnpj,
                        "razaoSocial", nome,
                        "valorChamado", 150.00,
                        "valorKm", 2.50,
                        "tipoFluxoPagamento", "MENSAL",
                        "endereco", Map.of(
                                "cep", "64000-000",
                                "logradouro", "Av. Principal",
                                "numero", "100",
                                "bairro", "Centro",
                                "cidade", "Teresina",
                                "estado", "PI"
                        )
                ))
                .when()
                .post("/api/clientes");
    }

    private String resolverPath(String path) {
        if (path.contains("{id}") && ctx.getClienteId() != null) {
            return path.replace("{id}", ctx.getClienteId().toString());
        }
        return path;
    }

    private String gerarCnpj(int index) {
        return String.format("%02d.345.678/0001-90", index + 10);
    }
}
