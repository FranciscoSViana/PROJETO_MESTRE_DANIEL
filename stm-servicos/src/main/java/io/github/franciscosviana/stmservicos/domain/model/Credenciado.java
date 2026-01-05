package io.github.franciscosviana.stmservicos.domain.model;

import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPessoa;
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
public class Credenciado {

    @Id
    private UUID id;

    @Column(unique = true)
    private Long codigo;

    private String rag;

    @Enumerated(EnumType.STRING)
    private TipoPessoa tipoPessoa;
    private String numeroPessoa;

    private BigDecimal valorChamado;
    private BigDecimal valorKm;

    @Column(name = "quantidade_os_atendidas")
    private int quantidadeOsAtendidas;

    private String contato;
    private String telefones;
    private String email;

    @OneToMany(mappedBy = "credenciado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tecnico> tecnicos = new ArrayList<>();

    @Embedded
    private Endereco endereco;

    @Embedded
    private GeoLocation geoLocation;
}
