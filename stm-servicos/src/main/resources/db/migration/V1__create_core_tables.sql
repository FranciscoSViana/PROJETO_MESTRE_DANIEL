-- =========================
-- CLIENTE
-- =========================
CREATE TABLE cliente
(
    id                 UUID PRIMARY KEY,
    codigo             BIGINT UNIQUE,
    nome               VARCHAR(255),
    valor_chamado      NUMERIC(19, 2),
    valor_km           NUMERIC(19, 2),
    cnpj               VARCHAR(20),
    inscricao_estadual VARCHAR(50),
    razao_social       VARCHAR(255)
);

-- =========================
-- CREDENCIADO
-- =========================
CREATE TABLE credenciado
(
    id                      UUID PRIMARY KEY,
    codigo                  BIGINT UNIQUE,
    rag                     VARCHAR(50),
    tipo_pessoa             VARCHAR(20),
    numero_pessoa           VARCHAR(30),
    valor_chamado           NUMERIC(19, 2),
    valor_km                NUMERIC(19, 2),
    quantidade_os_atendidas INTEGER,
    contato                 VARCHAR(255),
    telefones               VARCHAR(255),
    email                   VARCHAR(255),

    endereco_cep            VARCHAR(20),
    endereco_logradouro     VARCHAR(255),
    endereco_numero         VARCHAR(20),
    endereco_complemento    VARCHAR(255),
    endereco_bairro         VARCHAR(255),
    endereco_cidade         VARCHAR(255),
    endereco_estado         VARCHAR(50)
);

-- =========================
-- CONTRATO
-- =========================
CREATE TABLE contrato
(
    id              UUID PRIMARY KEY,
    numero_contrato VARCHAR(100) NOT NULL,
    cliente_id      UUID         NOT NULL,
    CONSTRAINT fk_contrato_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente (id)
);

-- =========================
-- TECNICO
-- =========================
CREATE TABLE tecnico
(
    id                   UUID PRIMARY KEY,
    codigo               BIGINT UNIQUE,
    nome                 VARCHAR(255),
    cpf                  VARCHAR(20),
    telefone             VARCHAR(50),
    email                VARCHAR(255),
    credenciado_id       UUID NOT NULL,

    endereco_cep         VARCHAR(20),
    endereco_logradouro  VARCHAR(255),
    endereco_numero      VARCHAR(20),
    endereco_complemento VARCHAR(255),
    endereco_bairro      VARCHAR(255),
    endereco_cidade      VARCHAR(255),
    endereco_estado      VARCHAR(50),

    CONSTRAINT fk_tecnico_credenciado
        FOREIGN KEY (credenciado_id) REFERENCES credenciado (id)
);

-- =========================
-- CIDADE
-- =========================
CREATE TABLE cidade
(
    id             BIGSERIAL PRIMARY KEY,
    nome           VARCHAR(255),
    base           VARCHAR(50),
    km_ida         NUMERIC(19, 2),
    credenciado_id UUID,
    CONSTRAINT fk_cidade_credenciado
        FOREIGN KEY (credenciado_id) REFERENCES credenciado (id)
);

-- =========================
-- PEDAGIO
-- =========================
CREATE TABLE pedagio
(
    id        BIGSERIAL PRIMARY KEY,
    descricao VARCHAR(255),
    valor     NUMERIC(19, 2),
    cidade_id BIGINT,
    CONSTRAINT fk_pedagio_cidade
        FOREIGN KEY (cidade_id) REFERENCES cidade (id)
);

-- =========================
-- ORDEM DE SERVICO
-- =========================
CREATE TABLE ordem_servico
(
    id                   UUID PRIMARY KEY,
    os_clt               VARCHAR(100),
    osg                  VARCHAR(100) UNIQUE,
    status               VARCHAR(30),
    data_hora            TIMESTAMP WITH TIME ZONE,

    cliente_id           UUID,
    credenciado_id       UUID,
    contrato_id          UUID NOT NULL,

    contato              VARCHAR(255),
    departamento         VARCHAR(255),
    telefone             VARCHAR(50),

    endereco_cep         VARCHAR(20),
    endereco_logradouro  VARCHAR(255),
    endereco_numero      VARCHAR(20),
    endereco_complemento VARCHAR(255),
    endereco_bairro      VARCHAR(255),
    endereco_cidade      VARCHAR(255),
    endereco_estado      VARCHAR(50),

    acionador            VARCHAR(255),
    equipamento          VARCHAR(255),
    serie                VARCHAR(255),
    pib                  VARCHAR(255),
    defeito              TEXT,
    rastreio             VARCHAR(255),

    CONSTRAINT fk_os_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_os_credenciado FOREIGN KEY (credenciado_id) REFERENCES credenciado (id),
    CONSTRAINT fk_os_contrato FOREIGN KEY (contrato_id) REFERENCES contrato (id)
);

-- =========================
-- SOLUCAO OS
-- =========================
CREATE TABLE solucao_os
(
    id               BIGSERIAL PRIMARY KEY,
    ordem_servico_id UUID UNIQUE,
    tecnico          VARCHAR(255),
    data_visita      TIMESTAMP WITH TIME ZONE,
    inicio           TIMESTAMP WITH TIME ZONE,
    termino          TIMESTAMP WITH TIME ZONE,
    solucao          TEXT,
    km               NUMERIC(19, 2),
    pedagios         NUMERIC(19, 2),
    estac            NUMERIC(19, 2),
    outros           TEXT,
    CONSTRAINT fk_solucao_os
        FOREIGN KEY (ordem_servico_id) REFERENCES ordem_servico (id)
);

-- =========================
-- FATURAMENTO OS
-- =========================
CREATE TABLE faturamento_os
(
    id               UUID PRIMARY KEY,
    ordem_servico_id UUID UNIQUE,

    cliente          VARCHAR(255),
    contrato         VARCHAR(255),
    status_os        VARCHAR(50),
    rag              VARCHAR(50),

    chamado          VARCHAR(255),
    deslocamento     NUMERIC(19, 2),
    ttl_km           NUMERIC(19, 2),
    pedagios         NUMERIC(19, 2),
    estac            NUMERIC(19, 2),
    outros           TEXT,

    total            NUMERIC(19, 2),
    doc              VARCHAR(255),
    status           VARCHAR(50),
    km               NUMERIC(19, 2),
    total_geral      NUMERIC(19, 2),
    nota_fiscal      VARCHAR(255),

    faturado         NUMERIC(19, 2),
    saldo_mo         NUMERIC(19, 2),
    saldo_km         NUMERIC(19, 2),
    saldo_outros     NUMERIC(19, 2),
    imposto          NUMERIC(19, 2),
    saldo_total      NUMERIC(19, 2),

    CONSTRAINT fk_faturamento_os
        FOREIGN KEY (ordem_servico_id) REFERENCES ordem_servico (id)
);

-- =========================
-- CONTROLE FATURAMENTO
-- =========================
CREATE TABLE controle_faturamento
(
    id          UUID PRIMARY KEY,
    emissao     TIMESTAMP WITH TIME ZONE NOT NULL,
    cliente_id  UUID,
    planilha VARCHAR(255),
    nota_fiscal VARCHAR(255),
    valor_nf    NUMERIC(19, 2),
    vl_medio    NUMERIC(19, 2),
    previsao    TIMESTAMP WITH TIME ZONE,
    pagamento   TIMESTAMP WITH TIME ZONE,
    custos      NUMERIC(19, 2),
    imposto     NUMERIC(19, 2),
    lucro       NUMERIC(19, 2),
    lucro_medio NUMERIC(19, 2),
    CONSTRAINT fk_controle_cliente
        FOREIGN KEY (cliente_id) REFERENCES cliente (id)
);

-- =========================
-- USUARIO
-- =========================
CREATE TABLE usuario
(
    id         UUID PRIMARY KEY,
    nome       VARCHAR(255)        NOT NULL,
    email      VARCHAR(255) UNIQUE NOT NULL,
    senha      VARCHAR(255)        NOT NULL,
    enabled    BOOLEAN,
    created_at TIMESTAMP
);

CREATE TABLE usuario_roles
(
    usuario_id UUID NOT NULL,
    role_name  VARCHAR(50),
    CONSTRAINT fk_usuario_roles
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

-- =========================
-- HISTORICO SENHA
-- =========================
CREATE TABLE historico_senha
(
    id         UUID PRIMARY KEY,
    usuario_id UUID         NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    criada_em  TIMESTAMP    NOT NULL,
    CONSTRAINT fk_hist_senha_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

-- =========================
-- RESET TOKEN
-- =========================
CREATE TABLE senha_reset_token
(
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(255),
    expiry_date TIMESTAMP,
    usuario_id  UUID,
    CONSTRAINT fk_reset_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);
