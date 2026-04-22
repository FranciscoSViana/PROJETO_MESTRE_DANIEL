-- V21__adicionar_pago_corrigido_pagamento_cliente_os.sql

ALTER TABLE pagamento_cliente_os
    ADD COLUMN IF NOT EXISTS pago BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS corrigido BOOLEAN NOT NULL DEFAULT FALSE;