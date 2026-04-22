ALTER TABLE ordem_servico
DROP
CONSTRAINT IF EXISTS chk_status_rastreio;

ALTER TABLE ordem_servico
    ADD CONSTRAINT chk_status_rastreio
        CHECK (status_rastreio IN (
                                   'POSTADO',
                                   'A_CAMINHO',
                                   'SAIU_PARA_ENTREGA',
                                   'CHEGOU',
                                   'DEVOLVIDO',
                                   'AGUARDANDO'
            ));