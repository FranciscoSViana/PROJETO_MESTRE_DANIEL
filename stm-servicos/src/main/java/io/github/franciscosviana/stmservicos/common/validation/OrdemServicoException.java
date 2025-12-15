package io.github.franciscosviana.stmservicos.common.validation;

public class OrdemServicoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public OrdemServicoException(String mensagem) {
        super(mensagem);
    }
}
