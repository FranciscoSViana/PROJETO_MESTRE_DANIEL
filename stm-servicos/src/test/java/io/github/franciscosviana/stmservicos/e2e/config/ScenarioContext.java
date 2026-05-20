package io.github.franciscosviana.stmservicos.e2e.config;

import io.restassured.response.Response;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Scope("cucumber-glue")
public class ScenarioContext {

    private String authToken;
    private String refreshToken;
    private Response lastResponse;
    private UUID lastCreatedId;
    private UUID clienteId;
    private UUID credenciadoId;
    private UUID contratoId;
    private UUID osId;
    private final Map<String, Object> dados = new HashMap<>();

    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public Response getLastResponse() { return lastResponse; }
    public void setLastResponse(Response lastResponse) { this.lastResponse = lastResponse; }

    public UUID getLastCreatedId() { return lastCreatedId; }
    public void setLastCreatedId(UUID lastCreatedId) { this.lastCreatedId = lastCreatedId; }

    public UUID getClienteId() { return clienteId; }
    public void setClienteId(UUID clienteId) { this.clienteId = clienteId; }

    public UUID getCredenciadoId() { return credenciadoId; }
    public void setCredenciadoId(UUID credenciadoId) { this.credenciadoId = credenciadoId; }

    public UUID getContratoId() { return contratoId; }
    public void setContratoId(UUID contratoId) { this.contratoId = contratoId; }

    public UUID getOsId() { return osId; }
    public void setOsId(UUID osId) { this.osId = osId; }

    public void put(String key, Object value) { dados.put(key, value); }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) { return (T) dados.get(key); }

    public void limpar() {
        authToken = null;
        refreshToken = null;
        lastResponse = null;
        lastCreatedId = null;
        clienteId = null;
        credenciadoId = null;
        contratoId = null;
        osId = null;
        dados.clear();
    }
}
