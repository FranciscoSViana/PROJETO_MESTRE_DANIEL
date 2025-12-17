package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.Credenciado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CredenciadoRepository extends JpaRepository<Credenciado, UUID> {

    @Query("SELECT COALESCE(MAX(c.codigo), 0) FROM Credenciado c")
    Long buscarUltimoCodigo();

    Optional<Credenciado> findByCodigo(Long codigo);

    @Query(value = """
                SELECT c.*
                FROM credenciado c
                WHERE (
                    6371 * acos(
                        cos(radians(:lat)) * cos(radians(c.latitude)) *
                        cos(radians(c.longitude) - radians(:lng)) +
                        sin(radians(:lat)) * sin(radians(c.latitude))
                    )
                ) <= :raio
            """, nativeQuery = true)
    List<Credenciado> buscarPorRaio(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("raio") double raio
    );

}
