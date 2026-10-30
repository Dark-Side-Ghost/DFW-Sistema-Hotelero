package com.udb.hotel.service;

import com.udb.hotel.dto.CatalogoDtos.PoliticaRequest;
import com.udb.hotel.entity.Hotel;
import com.udb.hotel.entity.PoliticaCancelacion;
import com.udb.hotel.exception.ApiException;
import com.udb.hotel.repository.HotelRepository;
import com.udb.hotel.repository.PoliticaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PoliticaService {

    private final PoliticaRepository repo;
    private final HotelRepository hotelRepo;

    public List<PoliticaCancelacion> listar() {
        return repo.findAll();
    }

    public PoliticaCancelacion obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> ApiException.notFound("La política con id " + id + " no existe."));
    }

    @Transactional
    public PoliticaCancelacion crear(PoliticaRequest req) {
        return guardar(new PoliticaCancelacion(), req);
    }

    @Transactional
    public PoliticaCancelacion actualizar(Long id, PoliticaRequest req) {
        return guardar(obtener(id), req);
    }

    @Transactional
    public void eliminar(Long id) {
        repo.delete(obtener(id));
        repo.flush();
    }

    private PoliticaCancelacion guardar(PoliticaCancelacion p, PoliticaRequest req) {
        Hotel hotel = hotelRepo.findById(req.idHotel())
                .orElseThrow(() -> ApiException.notFound("El hotel indicado no existe."));
        p.setHotel(hotel);
        p.setDiasPreviosLimite(req.diasPreviosLimite());
        p.setPorcentajeCargo(req.porcentajeCargo());
        return repo.save(p);
    }
}
