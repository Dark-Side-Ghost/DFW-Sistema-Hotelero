package com.udb.hotel.service;

import com.udb.hotel.entity.TipoHabitacion;
import com.udb.hotel.exception.ApiException;
import com.udb.hotel.repository.TipoHabitacionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TipoHabitacionService {

    private final TipoHabitacionRepository repo;

    public List<TipoHabitacion> listar() {
        return repo.findAll(Sort.by("capacidadMaxima"));
    }

    public TipoHabitacion obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> ApiException.notFound("El tipo de habitación con id " + id + " no existe."));
    }

    @Transactional
    public TipoHabitacion crear(TipoHabitacion datos) {
        datos.setId(null);
        return repo.save(datos);
    }

    @Transactional
    public TipoHabitacion actualizar(Long id, TipoHabitacion datos) {
        TipoHabitacion t = obtener(id);
        t.setNombre(datos.getNombre());
        t.setCapacidadMaxima(datos.getCapacidadMaxima());
        return repo.save(t);
    }

    @Transactional
    public void eliminar(Long id) {
        repo.delete(obtener(id));
        repo.flush();
    }
}
