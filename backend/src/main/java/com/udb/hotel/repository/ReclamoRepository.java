package com.udb.hotel.repository;

import com.udb.hotel.entity.Reclamo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReclamoRepository extends JpaRepository<Reclamo, Long> {
    List<Reclamo> findAllByOrderByIdDesc();

    List<Reclamo> findByReserva_Usuario_EmailOrderByIdDesc(String email);
}
