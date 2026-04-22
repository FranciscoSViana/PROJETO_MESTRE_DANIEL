package io.github.franciscosviana.stmservicos.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatusRastreio {

    POSTADO("Postado", "#3B82F6"),          // azul
    A_CAMINHO("A caminho", "#F59E0B"),      // amarelo/âmbar
    SAIU_PARA_ENTREGA("Saiu para entrega", "#F97316"), // laranja
    CHEGOU("Chegou", "#10B981"),            // verde
    DEVOLVIDO("Devolvido", "#EF4444"),      // vermelho
    AGUARDANDO("Aguardando", "#8B5CF6");    // roxo

    private final String descricao;
    private final String cor;

    public static StatusRastreio fromDescricao(String valor) {
        for (StatusRastreio status : values()) {
            if (status.getDescricao().equalsIgnoreCase(valor)
                    || status.name().equalsIgnoreCase(valor)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status rastreio inválido: " + valor);
    }
}
