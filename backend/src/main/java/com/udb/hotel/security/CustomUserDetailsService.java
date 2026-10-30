package com.udb.hotel.security;

import com.udb.hotel.entity.Usuario;
import com.udb.hotel.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Carga el usuario desde la BD. El rol se convierte en la autoridad ROLE_ADMIN, ROLE_CLIENTE, etc. */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return User.withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .roles(u.getRol().getNombre())
                .disabled(!"ACTIVO".equals(u.getEstado()))
                .build();
    }
}
