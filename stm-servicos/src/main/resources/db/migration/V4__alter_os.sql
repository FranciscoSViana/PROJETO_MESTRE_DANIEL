-- 1️⃣ Adiciona a coluna SEM NOT NULL
ALTER TABLE ordem_servico
    ADD COLUMN tecnico_id UUID;

-- 2️⃣ Define um técnico padrão para OS antigas
-- (ajuste conforme sua regra)
UPDATE ordem_servico
SET tecnico_id = (
    SELECT id FROM tecnico LIMIT 1
    )
WHERE tecnico_id IS NULL;

-- 3️⃣ Agora sim torna obrigatória
ALTER TABLE ordem_servico
    ALTER COLUMN tecnico_id SET NOT NULL;

-- 4️⃣ Cria a FK
ALTER TABLE ordem_servico
    ADD CONSTRAINT fk_os_tecnico
        FOREIGN KEY (tecnico_id)
            REFERENCES tecnico(id);
