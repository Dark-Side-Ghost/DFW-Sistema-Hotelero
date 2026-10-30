package com.udb.hotel.util;

/** Expresiones regulares compartidas por las validaciones del servidor. */
public final class Reglas {
    private Reglas() {}

    public static final String NOMBRE = "^[\\p{L}][\\p{L} .\u0027-]*$";
    public static final String MSG_NOMBRE = "Solo se permiten letras, espacios, puntos, apóstrofes y guiones";
    public static final String CLAVE = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$";
    public static final String MSG_CLAVE = "La contraseña debe tener entre 8 y 64 caracteres, con al menos una letra y un número";
    public static final String TELEFONO = "^\\+?[0-9]{8,15}$";
    public static final String MSG_TELEFONO = "El teléfono debe tener entre 8 y 15 dígitos (puede iniciar con +)";
    public static final String TELEFONO_HOTEL = "^[0-9+\\- ]{8,20}$";
    public static final String NUMERO_HABITACION = "^[A-Za-z0-9-]{1,20}$";
}
