package io.github.franciscosviana.stmservicos.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControleFaturamento {

    @Id
    private UUID id;

    @Column(nullable = false)
    private OffsetDateTime emissao;

    @ManyToOne
    private Cliente cliente;

    @Column(name = "planilhaos")
    private String planilha;

    private String notaFiscal;

    @Column(name = "valor_nf")
    private BigDecimal valorNF;
    private BigDecimal vlMedio;

    private OffsetDateTime previsao;

    private OffsetDateTime pagamento;

    private BigDecimal custos;
    private BigDecimal imposto;
    private BigDecimal lucro;
    private BigDecimal lucroMedio;
}
