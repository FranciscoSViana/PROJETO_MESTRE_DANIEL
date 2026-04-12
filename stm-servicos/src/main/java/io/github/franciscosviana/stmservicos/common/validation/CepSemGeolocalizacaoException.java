package io.github.franciscosviana.stmservicos.common.validation;

public class CepSemGeolocalizacaoException extends RuntimeException {
    public CepSemGeolocalizacaoException(String cep) {
        super(cep);
    }
}

