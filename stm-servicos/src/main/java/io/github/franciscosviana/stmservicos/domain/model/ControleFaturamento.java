package io.github.franciscosviana.stmservicos.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    @Column(nullable = false, columnDefinition = "datetime")
    private OffsetDateTime emissao;

    @ManyToOne
    private Cliente cliente;

    private String planilhaOS;

    private String notaFiscal;
    private BigDecimal valorNF;
    private BigDecimal vlMedio;

    private OffsetDateTime previsao;

    private OffsetDateTime pagamento;

    private BigDecimal custos;
    private BigDecimal imposto;
    private BigDecimal lucro;
    private BigDecimal lucroMedio;
}
