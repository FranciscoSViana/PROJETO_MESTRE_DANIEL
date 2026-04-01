-- Adiciona campos ao usuario
ALTER TABLE usuario
    ADD COLUMN IF NOT EXISTS username VARCHAR(60) UNIQUE,
    ADD COLUMN IF NOT EXISTS nome_completo VARCHAR(150),
    ADD COLUMN IF NOT EXISTS data_nascimento DATE,
    ADD COLUMN IF NOT EXISTS ultima_alteracao_senha TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS notificacao_senha_enviada BOOLEAN DEFAULT FALSE;

-- Preenche username para usuários existentes (usa nome como base)
UPDATE usuario SET username = LOWER(REPLACE(nome, ' ', '.')) WHERE username IS NULL;

-- Torna username obrigatório depois de preencher
ALTER TABLE usuario ALTER COLUMN ultima_alteracao_senha SET DEFAULT NOW();

-- Tabela de notificações em tela
CREATE TABLE IF NOT EXISTS notificacao_usuario (
                                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    tipo VARCHAR(50) NOT NULL,
    mensagem TEXT NOT NULL,
    lida BOOLEAN NOT NULL DEFAULT FALSE,
    criada_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
    );