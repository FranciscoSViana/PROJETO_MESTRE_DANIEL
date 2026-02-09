package io.github.franciscosviana.stmservicos.common.client.model;

import lombok.Data;
import java.util.List;

@Data
public class GoogleGeocodeResponse {
    private List<Result> results;
    private String status;

    @Data
    public static class Result {
        private List<AddressComponent> address_components;
        private Geometry geometry;
        private String formatted_address;
    }

    @Data
    public static class AddressComponent {
        private String long_name;
        private String short_name;
        private List<String> types;
    }

    @Data
    public static class Geometry {
        private Location location;
        private String location_type;
    }

    @Data
    public static class Location {
        private double lat;
        private double lng;
    }
}