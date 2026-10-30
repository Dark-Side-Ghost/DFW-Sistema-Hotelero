package com.udb.hotel.service;

import com.udb.hotel.entity.Hotel;
import com.udb.hotel.exception.ApiException;
import com.udb.hotel.repository.HotelRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository repo;

    public List<Hotel> listar() {
        return repo.findAll(Sort.by("nombre"));
    }

    public Hotel obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> ApiException.notFound("El hotel con id " + id + " no existe."));
    }

    @Transactional
    public Hotel crear(Hotel datos) {
        datos.setId(null);
        return repo.save(datos);
    }

    @Transactional
    public Hotel actualizar(Long id, Hotel datos) {
        Hotel h = obtener(id);
        h.setNombre(datos.getNombre());
        h.setCiudad(datos.getCiudad());
        h.setPais(datos.getPais());
        h.setCategoria(datos.getCategoria());
        h.setDireccion(datos.getDireccion());
        h.setTelefono(datos.getTelefono());
        return repo.save(h);
    }

    @Transactional
    public void eliminar(Long id) {
        repo.delete(obtener(id));
        repo.flush();
    }
}
