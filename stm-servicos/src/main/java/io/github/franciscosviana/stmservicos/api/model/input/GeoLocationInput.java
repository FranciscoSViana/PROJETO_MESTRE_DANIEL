package io.github.franciscosviana.stmservicos.api.model.input;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocationInput {

    private Double latitude;
    private Double longitude;
}
