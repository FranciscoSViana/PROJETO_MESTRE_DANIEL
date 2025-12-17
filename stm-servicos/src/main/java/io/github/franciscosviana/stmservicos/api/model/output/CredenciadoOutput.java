package io.github.franciscosviana.stmservicos.api.model.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredenciadoOutput {

    private UUID id;
    private Long codigo;

    private String rag;
    private String tipoPessoa;
    private String numeroPessoa;

    private BigDecimal valorChamado;
    private BigDecimal valorKm;

    private int quantidadeOSAtendidas;

    private String contato;
    private String telefones;
    private String email;

    private List<TecnicoOutput> tecnicos;
    private EnderecoOutput endereco;

    private GeoLocationOutput geoLocation;
}
