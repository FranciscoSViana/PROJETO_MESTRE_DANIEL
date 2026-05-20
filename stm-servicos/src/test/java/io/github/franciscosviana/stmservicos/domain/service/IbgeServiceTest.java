package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.model.output.EstadoOutput;
import io.github.franciscosviana.stmservicos.api.model.output.MunicipioOutput;
import io.github.franciscosviana.stmservicos.common.client.BrasilAPIClient;
import io.github.franciscosviana.stmservicos.common.client.model.EstadoResponse;
import io.github.franciscosviana.stmservicos.common.client.model.MunicipioResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("IbgeService")
@ExtendWith(MockitoExtension.class)
class IbgeServiceTest {

    @Mock
    BrasilAPIClient brasilAPIClient;

    @InjectMocks
    IbgeService service;

    @Nested
    @DisplayName("listarEstados()")
    class ListarEstados {

        @Test
        @DisplayName("deve mapear EstadoResponse para EstadoOutput corretamente")
        void deveListarEstados() {
            EstadoResponse estado = new EstadoResponse();
            estado.setNome("Piauí");
            estado.setSigla("PI");

            when(brasilAPIClient.buscarEstados()).thenReturn(List.of(estado));

            List<EstadoOutput> result = service.listarEstados();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).nome()).isEqualTo("Piauí");
            assertThat(result.get(0).sigla()).isEqualTo("PI");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando BrasilAPI não retorna estados")
        void deveRetornarListaVazia() {
            when(brasilAPIClient.buscarEstados()).thenReturn(List.of());

            List<EstadoOutput> result = service.listarEstados();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("deve retornar todos os estados mapeados")
        void deveListarMultiplosEstados() {
            EstadoResponse pi = new EstadoResponse();
            pi.setNome("Piauí");
            pi.setSigla("PI");

            EstadoResponse sp = new EstadoResponse();
            sp.setNome("São Paulo");
            sp.setSigla("SP");

            when(brasilAPIClient.buscarEstados()).thenReturn(List.of(pi, sp));

            List<EstadoOutput> result = service.listarEstados();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(EstadoOutput::sigla)
                    .containsExactlyInAnyOrder("PI", "SP");
        }
    }

    @Nested
    @DisplayName("listarMunicipios()")
    class ListarMunicipios {

        @Test
        @DisplayName("deve mapear MunicipioResponse para MunicipioOutput")
        void deveListarMunicipios() {
            MunicipioResponse municipio = new MunicipioResponse();
            municipio.setNome("Teresina");
            municipio.setCodigo_ibge("2211001");

            when(brasilAPIClient.buscarMunicipios("PI")).thenReturn(List.of(municipio));

            List<MunicipioOutput> result = service.listarMunicipios("pi");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).nome()).isEqualTo("Teresina");
        }

        @Test
        @DisplayName("deve converter sigla para maiúsculo antes de consultar")
        void deveConverterSiglaParaMaiusculo() {
            when(brasilAPIClient.buscarMunicipios("PI")).thenReturn(List.of());

            service.listarMunicipios("pi");

            org.mockito.Mockito.verify(brasilAPIClient).buscarMunicipios("PI");
        }
    }
}
