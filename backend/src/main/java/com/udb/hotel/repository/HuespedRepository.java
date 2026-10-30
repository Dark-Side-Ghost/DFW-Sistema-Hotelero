package com.udb.hotel.repository;

import com.udb.hotel.entity.Huesped;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HuespedRepository extends JpaRepository<Huesped, Long> {
    Optional<Huesped> findByNumeroDocumento(String numeroDocumento);
}
