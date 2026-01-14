-- 1. Garantir extensão para UUID no PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. Ajustar coluna ID para geração automática (caso não exista default)
ALTER TABLE contrato
    ALTER COLUMN id SET DEFAULT uuid_generate_v4();

-- 3. Adicionar novas colunas
ALTER TABLE contrato
    ADD COLUMN nome_contrato VARCHAR(255),
    ADD COLUMN valor_chamado NUMERIC(19,2),
    ADD COLUMN valor_km NUMERIC(19,2),
    ADD COLUMN responsavel_contrato VARCHAR(255),
    ADD COLUMN telefone_contrato VARCHAR(50);

-- 4. Preencher valores padrão para registros antigos (ajuste se necessário)
UPDATE contrato
SET
    nome_contrato = 'Contrato sem nome',
    responsavel_contrato = 'Não informado',
    telefone_contrato = 'Não informado'
WHERE nome_contrato IS NULL;

-- 5. Aplicar NOT NULL conforme entidade
ALTER TABLE contrato
    ALTER COLUMN nome_contrato SET NOT NULL,
ALTER COLUMN responsavel_contrato SET NOT NULL,
    ALTER COLUMN telefone_contrato SET NOT NULL;
