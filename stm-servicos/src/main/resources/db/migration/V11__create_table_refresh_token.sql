CREATE TABLE refresh_token
(
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(255) NOT NULL UNIQUE,
    usuario_id  UUID         NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,

    CONSTRAINT fk_refresh_usuario
        FOREIGN KEY (usuario_id)
            REFERENCES usuario (id)
            ON DELETE CASCADE
);