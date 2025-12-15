package io.github.franciscosviana.stmservicos.domain.model.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum TipoPessoa {

    PESSOA_FISICA(1, "Pessoa física"),
    PESSOA_JURIDICA(2, "Pessoa jurídica");

    private Integer codigo;
    private String descricao;

    TipoPessoa(Integer codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public static TipoPessoa fromCodigo(Integer codigo) {
        return Arrays.stream(values())
                .filter(t -> t.getCodigo().equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Código inválido: " + codigo));
    }
}
