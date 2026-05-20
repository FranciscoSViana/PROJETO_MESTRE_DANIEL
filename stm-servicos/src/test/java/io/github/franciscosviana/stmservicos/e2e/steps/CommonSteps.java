package io.github.franciscosviana.stmservicos.e2e.steps;

import io.cucumber.java.pt.Entao;
import io.github.franciscosviana.stmservicos.e2e.config.ScenarioContext;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class CommonSteps {

    private final ScenarioContext ctx;

    @Entao("a resposta deve ter status {int}")
    public void respostaComStatus(int statusEsperado) {
        assertThat(ctx.getLastResponse().statusCode())
                .as("Status HTTP esperado: %d, mas foi: %d. Body: %s",
                        statusEsperado,
                        ctx.getLastResponse().statusCode(),
                        ctx.getLastResponse().body().asString())
                .isEqualTo(statusEsperado);
    }

    @Entao("a resposta deve conter o campo {string}")
    public void respostaComCampo(String campo) {
        Object valor = ctx.getLastResponse().jsonPath().get(campo);
        Assertions.<Object>assertThat(valor)
                .as("Campo '%s' esperado na resposta, mas não encontrado. Body: %s",
                        campo, ctx.getLastResponse().body().asString())
                .isNotNull();
    }

    @Entao("a resposta deve conter uma lista paginada")
    public void respostaComListaPaginada() {
        Object content = ctx.getLastResponse().jsonPath().get("content");
        Assertions.<Object>assertThat(content).isNotNull();
        assertThat(ctx.getLastResponse().jsonPath().getInt("totalElements")).isGreaterThanOrEqualTo(0);
    }

    @Entao("a resposta deve ser uma lista")
    public void respostaComLista() {
        Object body = ctx.getLastResponse().jsonPath().get();
        assertThat(body).isInstanceOf(java.util.List.class);
    }

    @Entao("a resposta deve conter uma mensagem de sucesso")
    public void respostaComMensagemSucesso() {
        assertThat(ctx.getLastResponse().jsonPath().getString("message")).isNotBlank();
    }

    @Entao("a resposta deve conter um {string}")
    public void respostaComCampoNaoVazio(String campo) {
        assertThat(ctx.getLastResponse().jsonPath().getString(campo))
                .as("Campo '%s' não encontrado ou vazio na resposta", campo)
                .isNotBlank();
    }

    @Entao("o tipo do token deve ser {string}")
    public void tipoDoToken(String tipo) {
        assertThat(ctx.getLastResponse().jsonPath().getString("tokenType")).isEqualTo(tipo);
    }

    @Entao("o content-type deve indicar planilha Excel")
    public void contentTypeExcel() {
        String contentType = ctx.getLastResponse().header("Content-Type");
        assertThat(contentType)
                .isNotNull()
                .contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
}
