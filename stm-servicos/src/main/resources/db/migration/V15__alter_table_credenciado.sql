ALTER TABLE credenciado
    ADD COLUMN tipo_fluxo_pagamento VARCHAR(20),
    ADD COLUMN chave_pix VARCHAR(100);

ALTER TABLE credenciado
DROP
COLUMN quantidade_os_atendidas;