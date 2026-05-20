package io.github.franciscosviana.stmservicos.e2e.steps;

import io.cucumber.java.pt.Quando;
import io.github.franciscosviana.stmservicos.e2e.config.ScenarioContext;
import lombok.RequiredArgsConstructor;

import static io.restassured.RestAssured.given;

@RequiredArgsConstructor
public class ContasSteps {

    private final ScenarioContext ctx;

    @Quando("eu faço GET em {string} para contas")
    public void getContas(String path) {
        ctx.setLastResponse(
                given()
                        .header("Authorization", "Bearer " + ctx.getAuthToken())
                        .when()
                        .get(path)
        );
    }
}
