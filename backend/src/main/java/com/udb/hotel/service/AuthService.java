package com.udb.hotel.service;

import com.udb.hotel.dto.AuthDtos.*;
import com.udb.hotel.entity.Usuario;
import com.udb.hotel.exception.ApiException;
import com.udb.hotel.repository.RolRepository;
import com.udb.hotel.repository.UsuarioRepository;
import com.udb.hotel.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest req) {
        Usuario u = usuarioRepository.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(() -> ApiException.unauthorized("Correo o contraseña incorrectos."));
        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            throw ApiException.unauthorized("Correo o contraseña incorrectos.");
        }
        if (!"ACTIVO".equals(u.getEstado())) {
            throw ApiException.forbidden("Tu cuenta está " + u.getEstado().toLowerCase() + ". Contacta al administrador.");
        }
        return respuesta(u);
    }

    /** Registro público: siempre crea un usuario con rol CLIENTE. */
    @Transactional
    public AuthResponse registro(RegistroRequest req) {
        String email = req.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw ApiException.conflict("Ya existe una cuenta con ese correo electrónico.");
        }
        Usuario u = new Usuario();
        u.setNombres(req.nombres().trim());
        u.setApellidos(req.apellidos().trim());
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setRol(rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> ApiException.badRequest("El rol CLIENTE no existe. Ejecuta el script de base de datos.")));
        u.setEstado("ACTIVO");
        return respuesta(usuarioRepository.save(u));
    }

    private AuthResponse respuesta(Usuario u) {
        String token = jwtService.generar(u.getEmail(), u.getRol().getNombre());
        return new AuthResponse(token, "Bearer", u.getId(), u.getNombres(), u.getApellidos(), u.getEmail(), u.getRol().getNombre());
    }
}
