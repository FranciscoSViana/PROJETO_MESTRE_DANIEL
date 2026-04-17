package io.github.franciscosviana.stmservicos.domain.model;

import io.github.franciscosviana.stmservicos.domain.model.enums.TipoFluxoPagamento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    private UUID id;

    @Column(unique = true)
    private Long codigo;

    private String nome;

    @Builder.Default
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contrato> contratos = new ArrayList<>();

    private BigDecimal valorChamado;
    private BigDecimal valorKm;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_fluxo_pagamento", length = 20)
    private TipoFluxoPagamento tipoFluxoPagamento;

    private String cnpj;
    private String inscricaoEstadual;
    private String razaoSocial;

    @Embedded
    private Endereco endereco;

    @PrePersist
    public void gerarId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}