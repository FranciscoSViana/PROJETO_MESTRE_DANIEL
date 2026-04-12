package io.github.franciscosviana.stmservicos.api.exceptionhandler;

import io.github.franciscosviana.stmservicos.common.validation.CPFInvalidoException;
import io.github.franciscosviana.stmservicos.common.validation.CepSemGeolocalizacaoException;
import io.github.franciscosviana.stmservicos.common.validation.UsuarioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiExceptionHandler")
class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    private WebRequest webRequest() {
        return new ServletWebRequest(new MockHttpServletRequest());
    }

    @Test
    @DisplayName("handleCPFInvalido deve retornar 400 com detalhe da exceção")
    void handleCpfInvalido() {
        ResponseEntity<Object> resp = handler.handleCPFInvalido(
                new CPFInvalidoException("CPF inválido"), webRequest());

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Problem body = (Problem) resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getDetail()).isEqualTo("CPF inválido");
        assertThat(body.getUserMessage()).isEqualTo("CPF inválido");
    }

    @Test
    @DisplayName("handleUsuario deve retornar 400 com mensagem do usuário")
    void handleUsuario() {
        ResponseEntity<Object> resp = handler.handleUsuario(
                new UsuarioException("E-mail já cadastrado"), webRequest());

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Problem body = (Problem) resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getDetail()).isEqualTo("E-mail já cadastrado");
    }

    @Test
    @DisplayName("handleUncaught deve retornar 500 com mensagem genérica")
    void handleUncaught() {
        ResponseEntity<Object> resp = handler.handleUncaught(
                new RuntimeException("boom"), webRequest());

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Problem body = (Problem) resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getDetail()).isEqualTo(ApiExceptionHandler.MSG_ERRO_GENERICA_USUARIO_FINAL);
    }

    @Test
    @DisplayName("handleCepSemGeo deve retornar 400 com campo 'erro'")
    void handleCepSemGeo() {
        ResponseEntity<?> resp = handler.handleCepSemGeo(
                new CepSemGeolocalizacaoException("CEP sem geo"));

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) resp.getBody();
        assertThat(body).containsEntry("erro", "CEP não possui geolocalização: CEP sem geo");
    }
}
