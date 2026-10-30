package com.udb.hotel.config;

import com.udb.hotel.entity.Rol;
import com.udb.hotel.entity.Usuario;
import com.udb.hotel.repository.RolRepository;
import com.udb.hotel.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Al iniciar crea los roles y 3 usuarios de prueba (si no existen). Así no hay que calcular hashes BCrypt a mano. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        for (String nombre : List.of("ADMIN", "RECEPCIONISTA", "CLIENTE")) {
            if (rolRepository.findByNombre(nombre).isEmpty()) {
                Rol r = new Rol();
                r.setNombre(nombre);
                rolRepository.save(r);
            }
        }
        crearUsuario("Administrador", "Del Sistema", "admin@hotel.com", "Admin123*", "ADMIN");
        crearUsuario("Douglas", "Hernández", "recepcion@hotel.com", "Recepcion123*", "RECEPCIONISTA");
        crearUsuario("María", "López", "cliente@hotel.com", "Cliente123*", "CLIENTE");
    }

    private void crearUsuario(String nombres, String apellidos, String email, String clave, String rol) {
        if (usuarioRepository.existsByEmail(email)) return;
        Usuario u = new Usuario();
        u.setNombres(nombres);
        u.setApellidos(apellidos);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(clave));
        u.setRol(rolRepository.findByNombre(rol).orElseThrow());
        u.setEstado("ACTIVO");
        usuarioRepository.save(u);
        log.info("Usuario de prueba creado: {} ({})", email, rol);
    }
}
