-- =========================
-- V1__update_embedded_columns.sql
-- =========================

-- =========================
-- TABELA CLIENTE
-- =========================
ALTER TABLE cliente
ADD COLUMN IF NOT EXISTS endereco_cep VARCHAR (20),
ADD COLUMN IF NOT EXISTS endereco_logradouro VARCHAR (255),
ADD COLUMN IF NOT EXISTS endereco_numero VARCHAR (20),
ADD COLUMN IF NOT EXISTS endereco_complemento VARCHAR (255),
ADD COLUMN IF NOT EXISTS endereco_bairro VARCHAR (255),
ADD COLUMN IF NOT EXISTS endereco_cidade VARCHAR (255),
ADD COLUMN IF NOT EXISTS endereco_estado VARCHAR (50);

-- =========================
-- TABELA CREDENCIADO
-- =========================
ALTER TABLE credenciado
ADD COLUMN IF NOT EXISTS endereco_cep VARCHAR (20),
ADD COLUMN IF NOT EXISTS endereco_logradouro VARCHAR (255),
ADD COLUMN IF NOT EXISTS endereco_numero VARCHAR (20),
ADD COLUMN IF NOT EXISTS endereco_complemento VARCHAR (255),
ADD COLUMN IF NOT EXISTS endereco_bairro VARCHAR (255),
ADD COLUMN IF NOT EXISTS endereco_cidade VARCHAR (255),
ADD COLUMN IF NOT EXISTS endereco_estado VARCHAR (50),
ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

-- =========================
-- TABELA TECNICO
-- =========================
ALTER TABLE tecnico
ADD COLUMN IF NOT EXISTS endereco_cep VARCHAR (20),
ADD COLUMN IF NOT EXISTS endereco_logradouro VARCHAR (255),
ADD COLUMN IF NOT EXISTS endereco_numero VARCHAR (20),
ADD COLUMN IF NOT EXISTS endereco_complemento VARCHAR (255),
ADD COLUMN IF NOT EXISTS endereco_bairro VARCHAR (255),
ADD COLUMN IF NOT EXISTS endereco_cidade VARCHAR (255),
ADD COLUMN IF NOT EXISTS endereco_estado VARCHAR (50);

-- =========================
-- TABELA ORDEM_SERVICO
-- =========================
ALTER TABLE ordem_servico
ADD COLUMN IF NOT EXISTS endereco_cep VARCHAR (20),
ADD COLUMN IF NOT EXISTS endereco_logradouro VARCHAR (255),
ADD COLUMN IF NOT EXISTS endereco_numero VARCHAR (20),
ADD COLUMN IF NOT EXISTS endereco_complemento VARCHAR (255),
ADD COLUMN IF NOT EXISTS endereco_bairro VARCHAR (255),
ADD COLUMN IF NOT EXISTS endereco_cidade VARCHAR (255),
ADD COLUMN IF NOT EXISTS endereco_estado VARCHAR (50);
