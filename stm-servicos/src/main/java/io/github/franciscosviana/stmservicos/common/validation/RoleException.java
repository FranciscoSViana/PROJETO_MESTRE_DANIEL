package io.github.franciscosviana.stmservicos.common.validation;

public class RoleException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public RoleException(String role) {
        super(role);
    }
}
