package com.udb.hotel.util;

import com.udb.hotel.exception.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Acceso sencillo al usuario autenticado (el que viene en el token JWT). */
public final class SecurityUtil {
    private SecurityUtil() {}

    private static Authentication auth() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) {
            throw ApiException.unauthorized("Debes iniciar sesión para continuar.");
        }
        return a;
    }

    public static String emailActual() {
        return auth().getName();
    }

    public static boolean tieneRol(String rol) {
        return auth().getAuthorities().stream().anyMatch(g -> g.getAuthority().equals("ROLE_" + rol));
    }

    /** Administrador o recepcionista. */
    public static boolean esStaff() {
        return tieneRol("ADMIN") || tieneRol("RECEPCIONISTA");
    }
}
