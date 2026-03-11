package io.github.franciscosviana.stmservicos.domain.service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Set;

@Slf4j
@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;

    public TokenService(
            @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}") String secret
    ) {

        SecretKey key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");

        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

    }

    public String generateToken(String usuario, Set<String> roles) {
        try {
            Instant now = Instant.now();

            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("stm-api")
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(1200))
                    .subject(usuario)
                    .claim("roles", roles)
                    .build();

            JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

            return jwtEncoder
                    .encode(JwtEncoderParameters.from(header, claims))
                    .getTokenValue();

        } catch (Exception e) {
            log.info(e.getMessage());
            throw e;
        }
    }
}
