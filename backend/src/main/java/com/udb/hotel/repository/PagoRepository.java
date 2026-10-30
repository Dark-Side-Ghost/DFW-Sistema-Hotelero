package com.udb.hotel.repository;

import com.udb.hotel.entity.Pago;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByReservaId(Long reservaId);

    List<Pago> findByEstado(String estado);
}
