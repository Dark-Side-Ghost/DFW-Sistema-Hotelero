package com.udb.hotel.service;

import com.udb.hotel.dto.UsuarioDtos.*;
import com.udb.hotel.entity.Rol;
import com.udb.hotel.entity.Usuario;
import com.udb.hotel.exception.ApiException;
import com.udb.hotel.repository.RolRepository;
import com.udb.hotel.repository.UsuarioRepository;
import com.udb.hotel.util.SecurityUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    /** Usuario que hizo la petición (según el token). */
    public Usuario actual() {
        return usuarioRepository.findByEmail(SecurityUtil.emailActual())
                .orElseThrow(() -> ApiException.unauthorized("Tu sesión no es válida. Inicia sesión nuevamente."));
    }

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(this::aRespuesta).toList();
    }

    public UsuarioResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest req) {
        if (req.password() == null || req.password().isBlank()) {
            throw ApiException.badRequest("La contraseña es obligatoria al crear un usuario.");
        }
        String email = req.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw ApiException.conflict("Ya existe un usuario con ese correo electrónico.");
        }
        Usuario u = new Usuario();
        aplicar(u, req, email);
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        return aRespuesta(usuarioRepository.save(u));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest req) {
        Usuario u = buscar(id);
        String email = req.email().trim().toLowerCase();
        usuarioRepository.findByEmail(email).filter(otro -> !otro.getId().equals(id)).ifPresent(otro -> {
            throw ApiException.conflict("Ya existe otro usuario con ese correo electrónico.");
        });
        String estado = req.estado() == null ? "ACTIVO" : req.estado();
        if (esMismoUsuario(u)) {
            if (!"ACTIVO".equals(estado)) throw ApiException.badRequest("No puedes desactivar tu propia cuenta.");
            if (!"ADMIN".equals(req.rol())) throw ApiException.badRequest("No puedes quitarte tu propio rol de administrador.");
        }
        aplicar(u, req, email);
        if (req.password() != null && !req.password().isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(req.password()));
        }
        return aRespuesta(usuarioRepository.save(u));
    }

    @Transactional
    public UsuarioResponse cambiarEstado(Long id, String estado) {
        Usuario u = buscar(id);
        if (esMismoUsuario(u) && !"ACTIVO".equals(estado)) {
            throw ApiException.badRequest("No puedes desactivar tu propia cuenta.");
        }
        u.setEstado(estado);
        return aRespuesta(usuarioRepository.save(u));
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario u = buscar(id);
        if (esMismoUsuario(u)) throw ApiException.badRequest("No puedes eliminar tu propia cuenta.");
        usuarioRepository.delete(u);
        usuarioRepository.flush();
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("El usuario con id " + id + " no existe."));
    }

    private boolean esMismoUsuario(Usuario u) {
        return u.getEmail().equalsIgnoreCase(SecurityUtil.emailActual());
    }

    private void aplicar(Usuario u, UsuarioRequest req, String email) {
        Rol rol = rolRepository.findByNombre(req.rol())
                .orElseThrow(() -> ApiException.badRequest("El rol indicado no existe."));
        u.setNombres(req.nombres().trim());
        u.setApellidos(req.apellidos().trim());
        u.setEmail(email);
        u.setRol(rol);
        u.setEstado(req.estado() == null ? "ACTIVO" : req.estado());
    }

    private UsuarioResponse aRespuesta(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombres(), u.getApellidos(), u.getEmail(), u.getRol().getNombre(), u.getEstado());
    }
}
