package io.github.franciscosviana.stmservicos.common.validation;

public class ClienteException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ClienteException(String mensagem) {
        super(mensagem);
    }
}
