package io.github.franciscosviana.stmservicos.domain.model.enums;

public enum TipoPagamento {

    BOLETO("Boleto"),
    PIX("Pix"),
    OUTROS("Outros");

    private final String descricao;

    TipoPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}