package io.github.franciscosviana.stmservicos.e2e.steps;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.github.franciscosviana.stmservicos.e2e.config.ScenarioContext;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class AuthSteps {

    private final ScenarioContext ctx;

    @Dado("que o usuário admin está autenticado")
    public void adminAutenticado() {
        Response resp = given()
                .contentType(ContentType.JSON)
                .body(Map.of("usuario", "admin", "senha", "admin"))
                .when()
                .post("/api/auth/login");

        assertThat(resp.statusCode())
                .as("Falha ao autenticar como admin. Body: " + resp.body().asString())
                .isEqualTo(200);
        ctx.setAuthToken(resp.jsonPath().getString("accessToken"));
        ctx.setRefreshToken(resp.jsonPath().getString("refreshToken"));
    }

    @Dado("que existe um usuário cadastrado com email {string} e username {string}")
    public void existeUsuarioCadastrado(String email, String username) {
        if (!"admin".equals(username)) {
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "nomeCompleto", "Usuário Teste",
                            "email", email,
                            "senha", "Senha@123456",
                            "dataNascimento", "1990-01-01"
                    ))
                    .when()
                    .post("/api/auth/cadastro");
        }
    }

    @Quando("eu faço POST em {string} com as credenciais do admin")
    public void loginAdmin(String path) {
        ctx.setLastResponse(
                given()
                        .contentType(ContentType.JSON)
                        .body(Map.of("usuario", "admin", "senha", "admin"))
                        .when()
                        .post(path)
        );
    }

    @Quando("eu faço POST em {string} com senha incorreta")
    public void loginSenhaIncorreta(String path) {
        ctx.setLastResponse(
                given()
                        .contentType(ContentType.JSON)
                        .body(Map.of("usuario", "admin", "senha", "senha-errada"))
                        .when()
                        .post(path)
        );
    }

    @Quando("eu faço POST em {string} com username {string} e senha {string}")
    public void loginComCredenciais(String path, String username, String senha) {
        ctx.setLastResponse(
                given()
                        .contentType(ContentType.JSON)
                        .body(Map.of("usuario", username, "senha", senha))
                        .when()
                        .post(path)
        );
    }

    @Quando("eu faço POST em {string} com dados de usuário válidos")
    public void cadastrarUsuarioValido(String path) {
        ctx.setLastResponse(
                given()
                        .contentType(ContentType.JSON)
                        .body(Map.of(
                                "nomeCompleto", "Novo Usuário",
                                "email", "novousuario@email.com",
                                "senha", "Senha@123456",
                                "dataNascimento", "1990-06-15"
                        ))
                        .when()
                        .post(path)
        );
    }

    @Quando("eu faço POST em {string} com email {string} duplicado")
    public void cadastrarEmailDuplicado(String path, String email) {
        ctx.setLastResponse(
                given()
                        .contentType(ContentType.JSON)
                        .body(Map.of(
                                "nomeCompleto", "Outro Usuário",
                                "email", email,
                                "senha", "Senha@123456",
                                "dataNascimento", "1990-01-01"
                        ))
                        .when()
                        .post(path)
        );
    }

    @Quando("eu faço POST em {string} com senha fraca {string}")
    public void cadastrarSenhaFraca(String path, String senha) {
        ctx.setLastResponse(
                given()
                        .contentType(ContentType.JSON)
                        .body(Map.of(
                                "nomeCompleto", "Usuário Fraco",
                                "email", "fraco@email.com",
                                "senha", senha,
                                "dataNascimento", "1990-01-01"
                        ))
                        .when()
                        .post(path)
        );
    }

    @Quando("eu faço POST em {string} com email {string}")
    public void esqueciSenha(String path, String email) {
        ctx.setLastResponse(
                given()
                        .contentType(ContentType.JSON)
                        .body(Map.of("email", email))
                        .when()
                        .post(path)
        );
    }

    @Quando("eu faço POST em {string} com o refresh token obtido no login")
    public void refreshToken(String path) {
        ctx.setLastResponse(
                given()
                        .contentType(ContentType.JSON)
                        .body(Map.of("refreshToken", ctx.getRefreshToken()))
                        .when()
                        .post(path)
        );
    }

    @Entao("a resposta deve conter um novo {string}")
    public void respostaComNovoToken(String campo) {
        assertThat(ctx.getLastResponse().jsonPath().getString(campo)).isNotBlank();
    }
}
