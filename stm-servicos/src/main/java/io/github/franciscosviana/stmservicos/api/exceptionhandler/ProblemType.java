package io.github.franciscosviana.stmservicos.api.exceptionhandler;

public enum ProblemType {

    ERRO_DE_SISTEMA("/erro-de-sistema", "Erro de sistema"),
    DADOS_INVALIDOS("/dados-invalidos", "Dados inválidos"),
    ERRO_NEGOCIO("/erro-negocio", "Erro de negócio"),
    RECURSO_NAO_ENCONTRADO("/recurso-nao-encontrado", "Recurso não encontrado");

    private final String uri;
    private final String title;

    ProblemType(String uri, String title) {
        this.uri = uri;
        this.title = title;
    }

    public String getUri() {
        return uri;
    }

    public String getTitle() {
        return title;
    }
}
