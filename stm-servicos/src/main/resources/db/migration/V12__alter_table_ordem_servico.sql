ALTER TABLE ordem_servico
    ADD COLUMN status_rastreio VARCHAR(20)
        CONSTRAINT chk_status_rastreio
            CHECK (status_rastreio IN ('POSTADO', 'A_CAMINHO', 'CHEGOU', 'DEVOLVIDO', 'AGUARDANDO'));