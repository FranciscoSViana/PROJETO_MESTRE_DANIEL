package io.github.franciscosviana.stmservicos.common.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Exceptions de domínio")
class DomainExceptionsTest {

    @Test
    @DisplayName("ClienteException deve guardar mensagem")
    void clienteException() {
        assertThatThrownBy(() -> {
            throw new ClienteException("msg");
        })
                .isInstanceOf(ClienteException.class).hasMessage("msg");
    }

    @Test
    @DisplayName("ContratoException deve guardar mensagem")
    void contratoException() {
        assertThatThrownBy(() -> {
            throw new ContratoException("msg");
        })
                .isInstanceOf(ContratoException.class).hasMessage("msg");
    }

    @Test
    @DisplayName("CredenciadoException deve guardar mensagem")
    void credenciadoException() {
        assertThatThrownBy(() -> {
            throw new CredenciadoException("msg");
        })
                .isInstanceOf(CredenciadoException.class).hasMessage("msg");
    }

    @Test
    @DisplayName("OrdemServicoException deve guardar mensagem")
    void ordemServicoException() {
        assertThatThrownBy(() -> {
            throw new OrdemServicoException("msg");
        })
                .isInstanceOf(OrdemServicoException.class).hasMessage("msg");
    }

    @Test
    @DisplayName("TecnicoException deve guardar mensagem")
    void tecnicoException() {
        assertThatThrownBy(() -> {
            throw new TecnicoException("msg");
        })
                .isInstanceOf(TecnicoException.class).hasMessage("msg");
    }

    @Test
    @DisplayName("UsuarioException deve guardar mensagem")
    void usuarioException() {
        assertThatThrownBy(() -> {
            throw new UsuarioException("msg");
        })
                .isInstanceOf(UsuarioException.class).hasMessage("msg");
    }

    @Test
    @DisplayName("SenhaFracaException deve guardar mensagem")
    void senhaFracaException() {
        assertThatThrownBy(() -> {
            throw new SenhaFracaException("senha fraca");
        })
                .isInstanceOf(SenhaFracaException.class).hasMessage("senha fraca");
    }

    @Test
    @DisplayName("SenhaRepetidaException deve guardar mensagem")
    void senhaRepetidaException() {
        assertThatThrownBy(() -> {
            throw new SenhaRepetidaException("repetida");
        })
                .isInstanceOf(SenhaRepetidaException.class).hasMessage("repetida");
    }

    @Test
    @DisplayName("RoleException deve guardar mensagem")
    void roleException() {
        assertThatThrownBy(() -> {
            throw new RoleException("role");
        })
                .isInstanceOf(RoleException.class).hasMessage("role");
    }

    @Test
    @DisplayName("CPFInvalidoException deve guardar mensagem")
    void cpfInvalidoException() {
        assertThatThrownBy(() -> {
            throw new CPFInvalidoException("cpf inválido");
        })
                .isInstanceOf(CPFInvalidoException.class).hasMessage("cpf inválido");
    }

    @Test
    @DisplayName("CepSemGeolocalizacaoException deve guardar mensagem")
    void cepSemGeoException() {
        assertThatThrownBy(() -> {
            throw new CepSemGeolocalizacaoException("sem geo");
        })
                .isInstanceOf(CepSemGeolocalizacaoException.class).hasMessage("sem geo");
    }

    @Test
    @DisplayName("CredenciadoSemGeolocalizacaoException deve montar mensagem com o UUID")
    void credenciadoSemGeoException() {
        java.util.UUID id = java.util.UUID.randomUUID();
        assertThatThrownBy(() -> {
            throw new CredenciadoSemGeolocalizacaoException(id);
        })
                .isInstanceOf(CredenciadoSemGeolocalizacaoException.class)
                .hasMessageContaining(id.toString())
                .hasMessageContaining("geolocalização");
    }

    @Test
    @DisplayName("Exceções são RuntimeException — não exigem checked throws")
    void todasSaoRuntimeException() {
        assertThat(new ClienteException("x")).isInstanceOf(RuntimeException.class);
        assertThat(new OrdemServicoException("x")).isInstanceOf(RuntimeException.class);
        assertThat(new SenhaFracaException("x")).isInstanceOf(RuntimeException.class);
    }
}
