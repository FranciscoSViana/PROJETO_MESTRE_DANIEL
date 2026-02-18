-- ===============================
-- 1. Remover colunas antigas
-- ===============================

ALTER TABLE solucao_os
DROP
COLUMN IF EXISTS tecnico,
    DROP
COLUMN IF EXISTS data_visita,
    DROP
COLUMN IF EXISTS inicio,
    DROP
COLUMN IF EXISTS termino;

-- ===============================
-- 2. Renomear colunas
-- ===============================

ALTER TABLE solucao_os
    RENAME COLUMN pedagios TO pedagio;

ALTER TABLE solucao_os
    RENAME COLUMN estac TO estacionamento;

-- ===============================
-- 3. Alterar tipo da coluna outros
-- ===============================

ALTER TABLE solucao_os
ALTER
COLUMN outros TYPE NUMERIC(19,2)
    USING NULLIF(outros, '')::NUMERIC(19,2);

-- ===============================
-- 4. Adicionar novas colunas
-- ===============================

ALTER TABLE solucao_os
    ADD COLUMN data_atendimento TIMESTAMP WITH TIME ZONE,
    ADD COLUMN hora_inicial TIMESTAMP WITH TIME ZONE,
    ADD COLUMN hora_final TIMESTAMP WITH TIME ZONE,
    ADD COLUMN peca_solicitada VARCHAR(255),
    ADD COLUMN observacao TEXT;

-- ===============================
-- 5. Garantir NOT NULL na FK
-- ===============================

ALTER TABLE solucao_os
    ALTER COLUMN ordem_servico_id SET NOT NULL;
