package io.github.franciscosviana.stmservicos.domain.model.enums;

public enum StatusOrdem {

    ABERTA(1, "Aberta"),
    CONCLUIDA(2, "Concluída"),
    CANCELADA(3, "Cancelada");

    private Integer codigo;
    private String descricao;

    StatusOrdem(Integer codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }
}
