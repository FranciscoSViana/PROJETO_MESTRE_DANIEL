CREATE TABLE historico_ordem_servico
(
    id               UUID PRIMARY KEY,
    ordem_servico_id UUID                     NOT NULL,
    usuario_id       UUID                     NOT NULL,
    acao             VARCHAR(50)              NOT NULL,
    descricao        TEXT,
    data_hora        TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE historico_ordem_servico
    ADD CONSTRAINT fk_historico_os_ordem_servico
        FOREIGN KEY (ordem_servico_id)
            REFERENCES ordem_servico (id);

ALTER TABLE historico_ordem_servico
    ADD CONSTRAINT fk_historico_os_usuario
        FOREIGN KEY (usuario_id)
            REFERENCES usuario (id);

CREATE INDEX idx_historico_os_ordem_servico
    ON historico_ordem_servico (ordem_servico_id);

CREATE INDEX idx_historico_os_data
    ON historico_ordem_servico (data_hora);
