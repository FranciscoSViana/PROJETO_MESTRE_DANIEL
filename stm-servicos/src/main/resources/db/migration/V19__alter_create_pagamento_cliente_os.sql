-- Adiciona tipo_fluxo_pagamento na tabela cliente
ALTER TABLE cliente
    ADD COLUMN IF NOT EXISTS tipo_fluxo_pagamento VARCHAR(20);

-- Cria tabela de pagamento do cliente (contas a receber)
CREATE TABLE IF NOT EXISTS pagamento_cliente_os (
                                                    id                  BIGSERIAL PRIMARY KEY,
                                                    ordem_servico_id    UUID        NOT NULL UNIQUE REFERENCES ordem_servico(id),
    os_clt              VARCHAR(100),
    osg                 VARCHAR(100),
    cliente             VARCHAR(255),
    contrato            VARCHAR(100),

    valor_chamado       NUMERIC(10, 2) NOT NULL DEFAULT 0,
    km                  NUMERIC(10, 2) NOT NULL DEFAULT 0,
    valor_km            NUMERIC(10, 2) NOT NULL DEFAULT 0,
    pedagio             NUMERIC(10, 2) NOT NULL DEFAULT 0,
    estacionamento      NUMERIC(10, 2) NOT NULL DEFAULT 0,
    outros              TEXT,
    valor_outros        NUMERIC(10, 2) NOT NULL DEFAULT 0,
    valor_total         NUMERIC(10, 2) NOT NULL DEFAULT 0,

    lote                VARCHAR(100),
    nf                  VARCHAR(50),
    tipo_pagamento      VARCHAR(50),
    banco               VARCHAR(100),
    url_comprovante     TEXT,
    data_prevista       DATE,
    data_pagamento      TIMESTAMPTZ,
    criado_em           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    recebido            BOOLEAN NOT NULL DEFAULT FALSE
    );