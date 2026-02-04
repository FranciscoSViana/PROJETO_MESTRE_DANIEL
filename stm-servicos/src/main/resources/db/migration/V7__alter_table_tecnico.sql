-- Remove colunas de endereço do técnico
ALTER TABLE tecnico
DROP COLUMN IF EXISTS endereco_cep,
    DROP COLUMN IF EXISTS endereco_logradouro,
    DROP COLUMN IF EXISTS endereco_numero,
    DROP COLUMN IF EXISTS endereco_complemento,
    DROP COLUMN IF EXISTS endereco_bairro,
    DROP COLUMN IF EXISTS endereco_cidade,
    DROP COLUMN IF EXISTS endereco_estado;

-- Garante que CPF continue opcional
ALTER TABLE tecnico
    ALTER COLUMN cpf DROP NOT NULL;
