CREATE TABLE pagamento_os
(
    id               BIGSERIAL PRIMARY KEY,

    ordem_servico_id UUID           NOT NULL UNIQUE
        REFERENCES ordem_servico (id),

    -- Snapshot da OS (desnormalizado)
    os_clt           VARCHAR(100),
    osg              VARCHAR(20),
    cliente          VARCHAR(255),
    contrato         VARCHAR(255),

    -- Valores
    valor_chamado    NUMERIC(10, 2) NOT NULL,
    km               NUMERIC(10, 2) NOT NULL,
    valor_km         NUMERIC(10, 2) NOT NULL,
    pedagio          NUMERIC(10, 2) NOT NULL,
    estacionamento   NUMERIC(10, 2) NOT NULL,
    outros           VARCHAR(255),
    valor_outros     NUMERIC(10, 2) NOT NULL,

    valor_total      NUMERIC(10, 2) NOT NULL,

    -- Pagamento
    lote             VARCHAR(50),
    cpf_nf           VARCHAR(20),
    tipo_pagamento   VARCHAR(50)    NOT NULL,
    banco            VARCHAR(100),
    url_comprovante  TEXT,

    data_pagamento   TIMESTAMPTZ,

    criado_em        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),

    -- 🔒 Proteções importantes
    CONSTRAINT chk_valores_nao_negativos CHECK (
        valor_chamado >= 0 AND
        km >= 0 AND
        valor_km >= 0 AND
        pedagio >= 0 AND
        estacionamento >= 0 AND
        valor_outros >= 0 AND
        valor_total >= 0
        )
);

-- Índice útil (consulta real)
CREATE INDEX idx_pagamento_os_data_pagamento
    ON pagamento_os (data_pagamento);