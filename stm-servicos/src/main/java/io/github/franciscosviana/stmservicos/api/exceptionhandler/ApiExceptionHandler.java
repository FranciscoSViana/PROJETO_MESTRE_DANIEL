package io.github.franciscosviana.stmservicos.api.exceptionhandler;

import io.github.franciscosviana.stmservicos.api.model.input.CampoErro;
import io.github.franciscosviana.stmservicos.common.validation.CPFInvalidoException;
import io.github.franciscosviana.stmservicos.common.validation.CepSemGeolocalizacaoException;
import io.github.franciscosviana.stmservicos.common.validation.CredenciaisInvalidasException;
import io.github.franciscosviana.stmservicos.common.validation.UsuarioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String MSG_ERRO_GENERICA_USUARIO_FINAL =
            "Ocorreu um erro interno inesperado no sistema. Tente novamente e se o problema persistir, entre em contato com o administrador do sistema.";

    // ============================
    // ✅ CPF INVÁLIDO
    // ============================
    @ExceptionHandler(CPFInvalidoException.class)
    public ResponseEntity<Object> handleCPFInvalido(CPFInvalidoException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemType problemType = ProblemType.ERRO_NEGOCIO;
        String detail = ex.getMessage();

        Problem problem = createProblemBuilder(status, problemType, detail)
                .userMessage(detail)
                .build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    // ============================
    // ✅ USUÁRIO (REGRA DE NEGÓCIO)
    // ============================
    @ExceptionHandler(UsuarioException.class)
    public ResponseEntity<Object> handleUsuario(UsuarioException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemType problemType = ProblemType.ERRO_NEGOCIO;
        String detail = ex.getMessage();

        Problem problem = createProblemBuilder(status, problemType, detail)
                .userMessage(detail)
                .build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    // ============================
    // ✅ ERRO GENÉRICO DO SISTEMA
    // ============================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUncaught(Exception ex, WebRequest request) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemType problemType = ProblemType.ERRO_DE_SISTEMA;
        String detail = MSG_ERRO_GENERICA_USUARIO_FINAL;

        log.error("Erro inesperado:", ex);

        Problem problem = createProblemBuilder(status, problemType, detail)
                .userMessage(detail)
                .build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    // ============================
    // ✅ VALIDAÇÃO DE DTO (@Valid)
    // ============================
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<CampoErro> erros = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new CampoErro(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        CampoErro erroSenha = erros.stream()
                .filter(c -> c.getCampo().equals("senha"))
                .findFirst()
                .orElse(null);

        Problem problem = createProblemBuilder(
                HttpStatus.BAD_REQUEST,
                ProblemType.ERRO_NEGOCIO,
                erroSenha != null ? erroSenha.getMensagem() : "Um ou mais campos estão inválidos."
        )
                .userMessage(erroSenha != null ? erroSenha.getMensagem() : "Preencha corretamente os campos destacados.")
                .fields(erros)
                .build();

        return handleExceptionInternal(ex, problem, headers, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(CepSemGeolocalizacaoException.class)
    public ResponseEntity<?> handleCepSemGeo(CepSemGeolocalizacaoException ex) {
        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "erro", "CEP não possui geolocalização: " + ex.getMessage()
                ));
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<Object> handleCredenciaisInvalidas(
            CredenciaisInvalidasException ex,
            WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemType problemType = ProblemType.ERRO_NEGOCIO;

        Problem problem = createProblemBuilder(status, problemType, ex.getMessage())
                .userMessage(ex.getMessage())
                .build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;  // ou UNAUTHORIZED (401) se preferir
        ProblemType problemType = ProblemType.ERRO_NEGOCIO;
        String detail = "Usuário ou senha inválidos.";

        Problem problem = createProblemBuilder(status, problemType, detail)
                .userMessage(detail)
                .build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    // ============================
    // ✅ BUILDER PADRÃO DO PROBLEM
    // ============================
    private Problem.ProblemBuilder createProblemBuilder(
            HttpStatus httpStatus,
            ProblemType problemType,
            String detail) {

        return Problem.builder()
                .timestamp(OffsetDateTime.now())
                .status(httpStatus.value())
                .type(problemType.getUri())
                .title(problemType.getTitle())
                .detail(detail);
    }
}
