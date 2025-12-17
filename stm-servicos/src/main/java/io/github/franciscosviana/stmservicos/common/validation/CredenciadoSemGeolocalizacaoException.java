package io.github.franciscosviana.stmservicos.common.validation;

import java.util.UUID;

public class CredenciadoSemGeolocalizacaoException extends RuntimeException {

    public CredenciadoSemGeolocalizacaoException(UUID id) {
        super("Credenciado " + id + " não possui geolocalização cadastrada");
    }
}

