package io.github.franciscosviana.stmservicos.e2e.steps;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.github.franciscosviana.stmservicos.e2e.config.ScenarioContext;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * Steps HTTP genéricos reutilizados por múltiplos domínios.
 * Responsável por GET e DELETE sem corpo específico.
 */
@RequiredArgsConstructor
public class HttpSteps {

    private final ScenarioContext ctx;

    @Dado("que não estou autenticado")
    public void naoAutenticado() {
        ctx.setAuthToken(null);
    }

    @Quando("eu faço GET em {string}")
    public void getEndpoint(String path) {
        String pathResolvido = resolverPath(path);
        RequestSpecification req = given();
        if (ctx.getAuthToken() != null) {
            req = req.header("Authorization", "Bearer " + ctx.getAuthToken());
        }
        ctx.setLastResponse(req.when().get(pathResolvido));
    }

    @Quando("eu faço DELETE em {string}")
    public void deleteEndpoint(String path) {
        String pathResolvido = resolverPath(path);
        ctx.setLastResponse(
                given()
                        .header("Authorization", "Bearer " + ctx.getAuthToken())
                        .when()
                        .delete(pathResolvido)
        );
    }

    private String resolverPath(String path) {
        String resultado = path;

        // Resolve {id} baseado no contexto do último recurso criado
        if (resultado.contains("{id}")) {
            UUID id = null;
            if (ctx.getOsId() != null && resultado.contains("ordens-servico")) {
                id = ctx.getOsId();
            } else if (ctx.getClienteId() != null && resultado.contains("clientes")) {
                id = ctx.getClienteId();
            } else if (ctx.getCredenciadoId() != null && resultado.contains("credenciados")) {
                id = ctx.getCredenciadoId();
            } else if (ctx.getOsId() != null) {
                id = ctx.getOsId();
            } else if (ctx.getClienteId() != null) {
                id = ctx.getClienteId();
            } else {
                id = UUID.fromString("00000000-0000-0000-0000-000000000000");
            }
            resultado = resultado.replace("{id}", id.toString());
        }

        if (resultado.contains("{clienteId}") && ctx.getClienteId() != null) {
            resultado = resultado.replace("{clienteId}", ctx.getClienteId().toString());
        }
        if (resultado.contains("{contratoId}") && ctx.getContratoId() != null) {
            resultado = resultado.replace("{contratoId}", ctx.getContratoId().toString());
        }
        if (resultado.contains("{credenciadoId}") && ctx.getCredenciadoId() != null) {
            resultado = resultado.replace("{credenciadoId}", ctx.getCredenciadoId().toString());
        }

        return resultado;
    }
}
