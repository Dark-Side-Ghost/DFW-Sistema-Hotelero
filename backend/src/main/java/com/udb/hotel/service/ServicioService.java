package com.udb.hotel.service;

import com.udb.hotel.entity.Servicio;
import com.udb.hotel.exception.ApiException;
import com.udb.hotel.repository.ServicioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository repo;

    public List<Servicio> listar() {
        return repo.findAll(Sort.by("nombre"));
    }

    public Servicio obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> ApiException.notFound("El servicio con id " + id + " no existe."));
    }

    @Transactional
    public Servicio crear(Servicio datos) {
        datos.setId(null);
        return repo.save(datos);
    }

    @Transactional
    public Servicio actualizar(Long id, Servicio datos) {
        Servicio s = obtener(id);
        s.setNombre(datos.getNombre());
        s.setCategoria(datos.getCategoria());
        s.setPrecio(datos.getPrecio());
        return repo.save(s);
    }

    @Transactional
    public void eliminar(Long id) {
        repo.delete(obtener(id));
        repo.flush();
    }
}
