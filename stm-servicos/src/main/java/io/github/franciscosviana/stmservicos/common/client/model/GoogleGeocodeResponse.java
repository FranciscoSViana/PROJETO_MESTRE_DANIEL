package io.github.franciscosviana.stmservicos.common.client.model;

import lombok.Data;

import java.util.List;

@Data
public class GoogleGeocodeResponse {

    private List<Result> results;

    @Data
    public static class Result {
        private Geometry geometry;
    }

    @Data
    public static class Geometry {
        private Location location;
        private String location_type;
    }

    @Data
    public static class Location {
        private Double lat;
        private Double lng;
    }
}
