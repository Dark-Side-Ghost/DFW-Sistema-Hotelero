package com.udb.hotel.repository;

import com.udb.hotel.entity.PoliticaCancelacion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PoliticaRepository extends JpaRepository<PoliticaCancelacion, Long> {
    Optional<PoliticaCancelacion> findFirstByHotelIdOrderByIdAsc(Long hotelId);
}
