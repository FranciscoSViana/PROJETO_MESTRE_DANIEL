package io.github.franciscosviana.stmservicos.common.validation;

public class TecnicoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TecnicoException(String mensagem) {
        super(mensagem);
    }
}
