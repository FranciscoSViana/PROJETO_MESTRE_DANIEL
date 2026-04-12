package io.github.franciscosviana.stmservicos.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PagamentoOS (domínio)")
class PagamentoOSTest {

    @Test
    @DisplayName("calcularValorTotal() deve somar todos os componentes corretamente")
    void deveCalcularValorTotalCompleto() {
        PagamentoOS pagamento = PagamentoOS.builder()
                .valorChamado(new BigDecimal("100.00"))
                .km(new BigDecimal("10"))
                .valorKm(new BigDecimal("2.50"))
                .pedagio(new BigDecimal("5.00"))
                .estacionamento(new BigDecimal("3.00"))
                .valorOutros(new BigDecimal("2.00"))
                .build();

        pagamento.calcularValorTotal();

        // 100 + (10*2.5) + 5 + 3 + 2 = 135
        assertThat(pagamento.getValorTotal()).isEqualByComparingTo("135.00");
    }

    @Test
    @DisplayName("calcularValorTotal() deve retornar apenas valorChamado quando demais são zero")
    void deveRetornarApenasValorChamado() {
        PagamentoOS pagamento = PagamentoOS.builder()
                .valorChamado(new BigDecimal("200.00"))
                .km(BigDecimal.ZERO)
                .valorKm(BigDecimal.ZERO)
                .pedagio(BigDecimal.ZERO)
                .estacionamento(BigDecimal.ZERO)
                .valorOutros(BigDecimal.ZERO)
                .build();

        pagamento.calcularValorTotal();

        assertThat(pagamento.getValorTotal()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("calcularValorTotal() deve tratar valores com casas decimais corretamente")
    void deveTratarCasasDecimais() {
        PagamentoOS pagamento = PagamentoOS.builder()
                .valorChamado(new BigDecimal("150.50"))
                .km(new BigDecimal("5.5"))
                .valorKm(new BigDecimal("1.80"))
                .pedagio(new BigDecimal("7.25"))
                .estacionamento(BigDecimal.ZERO)
                .valorOutros(BigDecimal.ZERO)
                .build();

        pagamento.calcularValorTotal();

        // 150.50 + (5.5 * 1.80) + 7.25 = 150.50 + 9.90 + 7.25 = 167.65
        assertThat(pagamento.getValorTotal()).isEqualByComparingTo("167.65");
    }

    @Test
    @DisplayName("pago deve ser false por padrão ao criar com builder")
    void pagoPadraoDeveFalse() {
        PagamentoOS pagamento = PagamentoOS.builder()
                .valorChamado(BigDecimal.ZERO)
                .km(BigDecimal.ZERO)
                .valorKm(BigDecimal.ZERO)
                .pedagio(BigDecimal.ZERO)
                .estacionamento(BigDecimal.ZERO)
                .valorOutros(BigDecimal.ZERO)
                .build();

        assertThat(pagamento.isPago()).isFalse();
    }
}
