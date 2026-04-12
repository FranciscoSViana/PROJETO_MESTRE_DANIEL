package io.github.franciscosviana.stmservicos.domain.model;

import io.github.franciscosviana.stmservicos.common.validation.OrdemServicoException;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrdemServico (domínio)")
class OrdemServicoTest {

    @Test
    @DisplayName("concluirCom() deve mudar status para CONCLUIDA e associar solução")
    void deveConcluirComSolucao() {
        OrdemServico os = new OrdemServico();
        SolucaoOS solucao = new SolucaoOS();

        os.concluirCom(solucao);

        assertThat(os.getStatus()).isEqualTo(StatusOrdem.CONCLUIDA);
        assertThat(os.getSolucao()).isEqualTo(solucao);
        assertThat(solucao.getOrdemServico()).isEqualTo(os);
    }

    @Test
    @DisplayName("concluirCom() deve lançar OrdemServicoException se já estiver CONCLUIDA")
    void deveLancarExcecaoSeJaConcluida() {
        OrdemServico os = new OrdemServico();
        os.setStatus(StatusOrdem.CONCLUIDA);

        assertThatThrownBy(() -> os.concluirCom(new SolucaoOS()))
                .isInstanceOf(OrdemServicoException.class)
                .hasMessageContaining("já concluída");
    }

    @Test
    @DisplayName("status padrão deve ser ABERTA ao criar nova OS")
    void statusPadraoDeveSerAberta() {
        OrdemServico os = new OrdemServico();
        assertThat(os.getStatus()).isEqualTo(StatusOrdem.ABERTA);
    }
}
