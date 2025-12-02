package io.github.franciscosviana.stmservicos.common.validation;

public class CPFInvalidoException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public  CPFInvalidoException(String message){
        super(message);
    }
}
