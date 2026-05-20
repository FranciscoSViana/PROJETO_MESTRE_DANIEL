package io.github.franciscosviana.stmservicos.e2e.steps;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.github.franciscosviana.stmservicos.e2e.config.ScenarioContext;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class OrdemServicoSteps {

    private final ScenarioContext ctx;

    @Dado("que existe a estrutura base de dados para OS")
    public void criarEstruturaBase() {
        // Cria cliente
        Response clienteResp = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "nome", "EMPRESA CLIENTE LTDA",
                        "cnpj", "11.222.333/0001-44",
                        "razaoSocial", "EMPRESA CLIENTE LTDA",
                        "valorChamado", 200.00,
                        "valorKm", 3.00,
                        "tipoFluxoPagamento", "MENSAL",
                        "endereco", Map.of(
                                "cep", "64000-000", "logradouro", "Av. Principal",
                                "numero", "100", "bairro", "Centro",
                                "cidade", "Teresina", "estado", "PI"
                        )
                ))
                .when().post("/api/clientes");
        assertThat(clienteResp.statusCode())
                .as("Falha ao criar cliente. Body: " + clienteResp.body().asString())
                .isEqualTo(200);
        ctx.setClienteId(UUID.fromString(clienteResp.jsonPath().getString("id")));

        // Cria contrato
        Response contratoResp = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "numeroContrato", "CONT-001",
                        "nomeContrato", "Contrato Principal",
                        "valorChamado", 200.00,
                        "valorKm", 3.00,
                        "responsavelContrato", "Responsável X",
                        "telefoneContrato", "(86) 99999-9999"
                ))
                .when()
                .post("/api/clientes/{clienteId}/contratos", ctx.getClienteId());
        assertThat(contratoResp.statusCode())
                .as("Falha ao criar contrato. Body: " + contratoResp.body().asString())
                .isEqualTo(200);
        ctx.setContratoId(UUID.fromString(contratoResp.jsonPath().getString("id")));

        // Cria credenciado
        Response credResp = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "rag", "CREDENCIADO TECNICO LTDA",
                        "tipoPessoa", "PESSOA_JURIDICA",
                        "numeroPessoa", "55.666.777/0001-88",
                        "valorChamado", 120.00,
                        "valorKm", 2.00,
                        "tipoFluxoPagamento", "POR_OS",
                        "contato", "Técnico João",
                        "email", "credenciado@email.com",
                        "endereco", Map.of(
                                "cep", "64000-000", "logradouro", "Rua Técnica",
                                "numero", "50", "bairro", "Norte",
                                "cidade", "Teresina", "estado", "PI"
                        )
                ))
                .when().post("/api/credenciados");
        assertThat(credResp.statusCode())
                .as("Falha ao criar credenciado. Body: " + credResp.body().asString())
                .isEqualTo(200);
        ctx.setCredenciadoId(UUID.fromString(credResp.jsonPath().getString("id")));

        // Cria técnico
        Response tecnicoResp = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "nome", "Técnico Silva",
                        "cpf", "529.982.247-25",
                        "telefone", "(86) 98888-7777",
                        "email", "tecnico@email.com"
                ))
                .when()
                .post("/api/credenciados/{credId}/tecnicos", ctx.getCredenciadoId());
        assertThat(tecnicoResp.statusCode())
                .as("Falha ao criar técnico. Body: " + tecnicoResp.body().asString())
                .isEqualTo(200);
        ctx.put("tecnicoId", UUID.fromString(tecnicoResp.jsonPath().getString("id")));
    }

    @Dado("que existe {int} ordem de serviço cadastrada")
    public void existeOrdemServico(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            Response resp = criarOS();
            assertThat(resp.statusCode())
                    .as("Falha ao criar OS. Body: " + resp.body().asString())
                    .isEqualTo(200);
            ctx.setOsId(UUID.fromString(resp.jsonPath().getString("id")));
        }
    }

    @Dado("que a OS já foi concluída")
    public void osConcluida() {
        existeOrdemServico(1);
        finalizarOS();
    }

    @Dado("que a OS foi concluída com solução registrada")
    public void osConcluidaComSolucao() {
        osConcluida();
    }

    @Dado("que existe 1 pagamento registrado para a OS")
    public void existePagamentoParaOs() {
        osConcluida();
    }

    @Quando("eu faço POST em {string} com dados de OS válidos")
    public void postOS(String path) {
        ctx.setLastResponse(criarOS());
    }

    @Quando("eu faço PATCH em {string} com status válido")
    public void patchRastreio(String path) {
        ctx.setLastResponse(
                given()
                        .header("Authorization", "Bearer " + ctx.getAuthToken())
                        .contentType(ContentType.JSON)
                        .body(Map.of("statusRastreio", "EM_ANDAMENTO"))
                        .when()
                        .patch(resolverOsPath(path))
        );
    }

    @Quando("eu faço POST em {string} com dados de solução válidos")
    public void postSolucao(String path) {
        ctx.setLastResponse(
                given()
                        .header("Authorization", "Bearer " + ctx.getAuthToken())
                        .contentType(ContentType.JSON)
                        .body(Map.of(
                                "dataAtendimento", "2026-05-20",
                                "horaInicial", "08:00",
                                "horaFinal", "12:00",
                                "solucao", "Equipamento substituído com sucesso.",
                                "km", 15.0,
                                "pedagio", 0.0,
                                "estacionamento", 0.0,
                                "outros", 0.0
                        ))
                        .when()
                        .post(resolverOsPath(path))
        );
    }

    @Quando("eu faço POST em {string} com dados de pagamento válidos")
    public void postPagamento(String path) {
        ctx.setLastResponse(
                given()
                        .header("Authorization", "Bearer " + ctx.getAuthToken())
                        .contentType(ContentType.JSON)
                        .body(Map.of(
                                "valorChamado", 120.00,
                                "km", 15.0,
                                "valorKm", 2.00,
                                "pedagio", 0.0,
                                "estacionamento", 0.0,
                                "valorOutros", 0.0,
                                "dataPagamento", "2026-05-20"
                        ))
                        .when()
                        .post(resolverOsPath(path))
        );
    }

    @Quando("eu faço PUT em {string} com dados de pagamento atualizados")
    public void putPagamento(String path) {
        ctx.setLastResponse(
                given()
                        .header("Authorization", "Bearer " + ctx.getAuthToken())
                        .contentType(ContentType.JSON)
                        .body(Map.of(
                                "valorChamado", 130.00,
                                "km", 20.0,
                                "valorKm", 2.00,
                                "pedagio", 5.0,
                                "estacionamento", 0.0,
                                "valorOutros", 0.0,
                                "dataPagamento", "2026-05-20"
                        ))
                        .when()
                        .put(resolverOsPath(path))
        );
    }

    @Quando("eu faço POST em {string} com dados de contrato válidos")
    public void postContrato(String path) {
        ctx.setLastResponse(
                given()
                        .header("Authorization", "Bearer " + ctx.getAuthToken())
                        .contentType(ContentType.JSON)
                        .body(Map.of(
                                "numeroContrato", "CONT-TESTE",
                                "nomeContrato", "Contrato Teste E2E",
                                "valorChamado", 200.00,
                                "valorKm", 3.00,
                                "responsavelContrato", "Responsável",
                                "telefoneContrato", "(86) 99999-9999"
                        ))
                        .when()
                        .post(resolverClientePath(path))
        );
        if (ctx.getLastResponse() != null && ctx.getLastResponse().statusCode() == 200) {
            ctx.setContratoId(UUID.fromString(ctx.getLastResponse().jsonPath().getString("id")));
        }
    }

    @Dado("que existe {int} contrato para o cliente")
    public void existeContrato(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            Response resp = given()
                    .header("Authorization", "Bearer " + ctx.getAuthToken())
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "numeroContrato", "CONT-00" + i,
                            "nomeContrato", "Contrato " + i,
                            "valorChamado", 200.00,
                            "valorKm", 3.00,
                            "responsavelContrato", "Resp",
                            "telefoneContrato", "(86) 99999-000" + i
                    ))
                    .when()
                    .post("/api/clientes/{clienteId}/contratos", ctx.getClienteId());
            assertThat(resp.statusCode()).isEqualTo(200);
            ctx.setContratoId(UUID.fromString(resp.jsonPath().getString("id")));
        }
    }

    private Response criarOS() {
        Map<String, Object> body = new HashMap<>();
        body.put("clienteId", ctx.getClienteId().toString());
        body.put("contratoId", ctx.getContratoId().toString());
        body.put("credenciadoId", ctx.getCredenciadoId().toString());
        body.put("tecnicoId", ctx.get("tecnicoId", UUID.class).toString());
        body.put("osClt", "OS-CLI-001");
        body.put("contato", "Contato X");
        body.put("departamento", "TI");
        body.put("telefone", "(86) 99999-0000");
        body.put("acionador", "Acionador Y");
        body.put("equipamento", "Notebook Dell");
        body.put("serie", "SN-12345");
        body.put("defeito", "Não liga");
        body.put("endereco", Map.of(
                "cep", "64000-000", "logradouro", "Av. Principal",
                "numero", "100", "bairro", "Centro",
                "cidade", "Teresina", "estado", "PI"
        ));
        return given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/ordens-servico");
    }

    private void finalizarOS() {
        Response resp = given()
                .header("Authorization", "Bearer " + ctx.getAuthToken())
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "dataAtendimento", "2026-05-20",
                        "horaInicial", "08:00",
                        "horaFinal", "12:00",
                        "solucao", "Problema resolvido.",
                        "km", 10.0,
                        "pedagio", 0.0,
                        "estacionamento", 0.0,
                        "outros", 0.0
                ))
                .when()
                .post("/api/ordens-servico/{id}/solucao", ctx.getOsId());

        assertThat(resp.statusCode())
                .as("Falha ao finalizar OS. Body: " + resp.body().asString())
                .isEqualTo(200);
    }

    private String resolverOsPath(String path) {
        return path.replace("{id}", ctx.getOsId() != null ?
                ctx.getOsId().toString() : UUID.randomUUID().toString());
    }

    private String resolverClientePath(String path) {
        return path.replace("{clienteId}", ctx.getClienteId() != null ?
                ctx.getClienteId().toString() : UUID.randomUUID().toString());
    }
}
