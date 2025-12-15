package io.github.franciscosviana.stmservicos.common.validation;

public class CredenciadoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CredenciadoException(String mensagem) {
        super(mensagem);
    }
}
