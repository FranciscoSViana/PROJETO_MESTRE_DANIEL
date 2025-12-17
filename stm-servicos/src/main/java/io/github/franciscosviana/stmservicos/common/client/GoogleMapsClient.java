package io.github.franciscosviana.stmservicos.common.client;

import io.github.franciscosviana.stmservicos.common.client.model.GoogleGeocodeResponse;
import io.github.franciscosviana.stmservicos.common.validation.CepSemGeolocalizacaoException;
import io.github.franciscosviana.stmservicos.domain.model.GeoLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleMapsClient {

    private static final String GOOGLE_MAPS_BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private final RestTemplate restTemplate;

    @Value("${google.maps.api-key}")
    private String apiKey;

    public GeoLocation buscarPorEndereco(String endereco) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                URLEncoder.encode(endereco, StandardCharsets.UTF_8) +
                "&components=country:BR&key=" + apiKey;

        GoogleGeocodeResponse response = restTemplate.getForObject(url, GoogleGeocodeResponse.class);

        if (response == null || response.getResults().isEmpty()) {
            throw new CepSemGeolocalizacaoException("Nenhum resultado para: " + endereco);
        }

        GoogleGeocodeResponse.Result result = response.getResults().get(0);

        GeoLocation geo = new GeoLocation(
                result.getGeometry().getLocation().getLat(),
                result.getGeometry().getLocation().getLng()
        );

        // Opcional: checar se é "APPROXIMATE" e apenas logar
        if ("APPROXIMATE".equals(result.getGeometry().getLocation_type())) {
            log.warn("Geolocalização aproximada para {}: lat={}, lng={}", endereco,
                    geo.getLatitude(), geo.getLongitude());
        }

        return geo;
    }

}

