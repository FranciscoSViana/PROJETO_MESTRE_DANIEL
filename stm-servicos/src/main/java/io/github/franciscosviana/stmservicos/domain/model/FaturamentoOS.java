package io.github.franciscosviana.stmservicos.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaturamentoOS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private OrdemServico ordemServico;

    private String cliente;
    private String contrato;
    private String statusOs;
    private String rag;

    private String chamado;
    private double deslocamento;
    private double ttlKm;
    private double pedagios;
    private double estac;
    private String outros;

    private double total;
    private String doc;
    private String status;

    private double km;

    private double totalGeral;
    private String notaFiscal;

    private double faturado;
    private double saldoMO;
    private double saldoKm;
    private double saldoOutros;
    private double imposto;
    private double saldoTotal;
}
