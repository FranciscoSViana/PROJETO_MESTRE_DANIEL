package io.github.franciscosviana.stmservicos.e2e.config;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class Hooks {

    private final JdbcTemplate jdbcTemplate;
    private final ScenarioContext scenarioContext;

    @Before
    public void antes() {
        limparDadosDeTeste();
        scenarioContext.limpar();
        WireMockConfig.resetStubs();
    }

    @After
    public void depois() {
        scenarioContext.limpar();
    }

    private void limparDadosDeTeste() {
        jdbcTemplate.execute("SET session_replication_role = replica");
        jdbcTemplate.execute("TRUNCATE TABLE historico_ordem_servico CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE pagamento_os CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE pagamento_cliente_os CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE solucao_os CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE faturamento_os CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE ordem_servico CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE contrato CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE tecnico CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE credenciado CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE cliente CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE historico_senha CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE refresh_token CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE senha_reset_token CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE notificacao_usuario CASCADE");
        // Preserva o usuário admin criado pelo AdminInitializer
        jdbcTemplate.execute("DELETE FROM usuario WHERE nome != 'admin'");
        jdbcTemplate.execute("SET session_replication_role = DEFAULT");
    }
}
