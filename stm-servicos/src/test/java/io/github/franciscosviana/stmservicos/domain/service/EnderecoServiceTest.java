package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.common.client.ViaCepClient;
import io.github.franciscosviana.stmservicos.common.client.model.ViaCepResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("EnderecoService")
@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock
    ViaCepClient viaCepClient;

    @InjectMocks
    EnderecoService service;

    @Nested
    @DisplayName("consultarCep()")
    class ConsultarCep {

        @Test
        @DisplayName("deve delegar para ViaCepClient e retornar resposta")
        void deveConsultarCep() {
            ViaCepResponse resp = new ViaCepResponse();
            when(viaCepClient.buscarEnderecoPorCep("64000-000")).thenReturn(resp);

            ViaCepResponse resultado = service.consultarCep("64000-000");

            assertThat(resultado).isEqualTo(resp);
            verify(viaCepClient).buscarEnderecoPorCep("64000-000");
        }

        @Test
        @DisplayName("deve retornar resposta com campos preenchidos")
        void deveRetornarCamposPreenchidos() {
            ViaCepResponse resp = new ViaCepResponse();
            resp.setCep("64000-000");
            resp.setLogradouro("Av. Frei Serafim");
            resp.setBairro("Centro");
            resp.setLocalidade("Teresina");
            resp.setUf("PI");

            when(viaCepClient.buscarEnderecoPorCep("64000-000")).thenReturn(resp);

            ViaCepResponse resultado = service.consultarCep("64000-000");

            assertThat(resultado.getCep()).isEqualTo("64000-000");
            assertThat(resultado.getLocalidade()).isEqualTo("Teresina");
            assertThat(resultado.getUf()).isEqualTo("PI");
        }
    }
}
