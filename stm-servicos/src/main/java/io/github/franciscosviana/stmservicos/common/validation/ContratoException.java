package io.github.franciscosviana.stmservicos.common.validation;

public class ContratoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ContratoException(String mensagem) {
        super(mensagem);
    }
}
