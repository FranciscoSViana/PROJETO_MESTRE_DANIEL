package io.github.franciscosviana.stmservicos.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControleFaturamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private OffsetDateTime emissao;

    @ManyToOne
    private Cliente cliente;

    private String planilhaOS;

    private String notaFiscal;
    private double valorNF;
    private double vlMedio;
    private OffsetDateTime previsao;
    private OffsetDateTime pagamento;

    private double custos;
    private double imposto;
    private double lucro;
    private double lucroMedio;
}
