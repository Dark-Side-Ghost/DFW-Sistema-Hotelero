-- =====================================================================
--  Sistema de Administración y Reserva de Habitaciones de Hotel (DWF404)
--  Script MySQL 8+  ->  crea la base, las 13 tablas y datos de ejemplo
--  EJECUTAR ANTES de iniciar la API. Si se vuelve a ejecutar, BORRA y recrea todo.
--  Los usuarios (admin, recepción, cliente) los crea la API al iniciar
--  (DataInitializer) con la contraseña ya encriptada con BCrypt.
-- =====================================================================
CREATE DATABASE IF NOT EXISTS hotel_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hotel_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS reclamos, pagos, reserva_servicios, reservas, habitacion_servicio, servicios,
    habitaciones, tipos_habitacion, politicas_cancelacion, hoteles, huespedes, usuarios, roles;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------- roles / usuarios
CREATE TABLE roles (
    id_rol     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE usuarios (
    id_usuario    BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_rol        BIGINT       NOT NULL,
    nombres       VARCHAR(100) NOT NULL,
    apellidos     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    estado        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES roles(id_rol),
    CONSTRAINT ck_usuario_estado CHECK (estado IN ('ACTIVO','INACTIVO','BLOQUEADO'))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------- huéspedes
CREATE TABLE huespedes (
    id_huesped         BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo    VARCHAR(200) NOT NULL,
    fecha_nacimiento   DATE         NOT NULL,
    tipo_documento     VARCHAR(30)  NOT NULL,
    numero_documento   VARCHAR(50)  NOT NULL UNIQUE,
    telefono           VARCHAR(20)  NOT NULL,
    correo_electronico VARCHAR(150) NOT NULL,
    CONSTRAINT ck_huesped_doc CHECK (tipo_documento IN ('DUI','PASAPORTE','DNI','NIT'))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------- hoteles y habitaciones
CREATE TABLE hoteles (
    id_hotel  BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre    VARCHAR(150) NOT NULL,
    ciudad    VARCHAR(100) NOT NULL,
    pais      VARCHAR(100) NOT NULL,
    categoria INT NULL,
    direccion VARCHAR(255) NULL,   -- añadida en la fase final (formulario "Agregar hotel")
    telefono  VARCHAR(20)  NULL,   -- añadida en la fase final
    CONSTRAINT ck_hotel_categoria CHECK (categoria BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE TABLE tipos_habitacion (
    id_tipo_habitacion BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre             VARCHAR(100) NOT NULL,
    capacidad_maxima   INT NOT NULL,
    CONSTRAINT ck_tipo_cap CHECK (capacidad_maxima > 0)
) ENGINE=InnoDB;

CREATE TABLE habitaciones (
    id_habitacion      BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_hotel           BIGINT        NOT NULL,
    id_tipo_habitacion BIGINT        NOT NULL,
    numero_habitacion  VARCHAR(20)   NOT NULL,
    precio_base_noche  DECIMAL(10,2) NOT NULL,
    estado             VARCHAR(20)   NOT NULL DEFAULT 'DISPONIBLE',
    CONSTRAINT fk_hab_hotel FOREIGN KEY (id_hotel) REFERENCES hoteles(id_hotel),
    CONSTRAINT fk_hab_tipo  FOREIGN KEY (id_tipo_habitacion) REFERENCES tipos_habitacion(id_tipo_habitacion),
    CONSTRAINT uq_hab_hotel_numero UNIQUE (id_hotel, numero_habitacion),
    CONSTRAINT ck_hab_precio CHECK (precio_base_noche >= 0),
    CONSTRAINT ck_hab_estado CHECK (estado IN ('DISPONIBLE','OCUPADA','MANTENIMIENTO'))
) ENGINE=InnoDB;

CREATE TABLE politicas_cancelacion (
    id_politica         BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_hotel            BIGINT       NOT NULL,
    dias_previos_limite INT          NOT NULL,
    porcentaje_cargo    DECIMAL(5,2) NOT NULL,
    CONSTRAINT fk_pol_hotel FOREIGN KEY (id_hotel) REFERENCES hoteles(id_hotel),
    CONSTRAINT ck_pol_dias CHECK (dias_previos_limite >= 0),
    CONSTRAINT ck_pol_porc CHECK (porcentaje_cargo BETWEEN 0 AND 100)
) ENGINE=InnoDB;

CREATE TABLE servicios (
    id_servicio BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100)  NOT NULL,
    categoria   VARCHAR(50)   NOT NULL,
    precio      DECIMAL(10,2) NOT NULL,
    CONSTRAINT ck_serv_precio CHECK (precio >= 0),
    CONSTRAINT ck_serv_cat CHECK (categoria IN ('ALIMENTACION','TRANSPORTE','LIMPIEZA','OTROS'))
) ENGINE=InnoDB;

CREATE TABLE habitacion_servicio (
    id_habitacion BIGINT  NOT NULL,
    id_servicio   BIGINT  NOT NULL,
    incluido      BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id_habitacion, id_servicio),
    CONSTRAINT fk_hs_hab  FOREIGN KEY (id_habitacion) REFERENCES habitaciones(id_habitacion),
    CONSTRAINT fk_hs_serv FOREIGN KEY (id_servicio)   REFERENCES servicios(id_servicio)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------- reservas, pagos, reclamos
CREATE TABLE reservas (
    id_reserva         BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_huesped         BIGINT        NOT NULL,
    id_habitacion      BIGINT        NOT NULL,
    id_usuario         BIGINT        NOT NULL,
    id_politica        BIGINT        NOT NULL,
    fecha_entrada      DATE          NOT NULL,
    fecha_salida       DATE          NOT NULL,
    numero_huespedes   INT           NOT NULL,
    precio_total       DECIMAL(10,2) NOT NULL,
    estado_reserva     VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',  -- se añadió EN_CURSO (check-in realizado)
    fecha_cancelacion  DATETIME      NULL,
    motivo_cancelacion TEXT          NULL,
    monto_reembolso    DECIMAL(10,2) NULL DEFAULT 0.00,
    fecha_creacion     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- añadida (estadísticas)
    CONSTRAINT fk_res_huesped FOREIGN KEY (id_huesped)    REFERENCES huespedes(id_huesped),
    CONSTRAINT fk_res_hab     FOREIGN KEY (id_habitacion) REFERENCES habitaciones(id_habitacion),
    CONSTRAINT fk_res_usuario FOREIGN KEY (id_usuario)    REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_res_pol     FOREIGN KEY (id_politica)   REFERENCES politicas_cancelacion(id_politica),
    CONSTRAINT ck_res_fechas  CHECK (fecha_salida > fecha_entrada),
    CONSTRAINT ck_res_huesp   CHECK (numero_huespedes > 0),
    CONSTRAINT ck_res_precio  CHECK (precio_total >= 0),
    CONSTRAINT ck_res_estado  CHECK (estado_reserva IN ('PENDIENTE','CONFIRMADA','EN_CURSO','FINALIZADA','CANCELADA'))
) ENGINE=InnoDB;

CREATE TABLE reserva_servicios (
    id_reserva_servicio BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_reserva          BIGINT        NOT NULL,
    id_servicio         BIGINT        NOT NULL,
    cantidad            INT           NOT NULL,
    subtotal            DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_rs_reserva FOREIGN KEY (id_reserva)  REFERENCES reservas(id_reserva),
    CONSTRAINT fk_rs_serv    FOREIGN KEY (id_servicio) REFERENCES servicios(id_servicio),
    CONSTRAINT ck_rs_cant CHECK (cantidad > 0),
    CONSTRAINT ck_rs_sub  CHECK (subtotal >= 0)
) ENGINE=InnoDB;

CREATE TABLE pagos (
    id_pago     BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_reserva  BIGINT        NOT NULL,
    metodo_pago VARCHAR(50)   NOT NULL,
    monto       DECIMAL(10,2) NOT NULL,
    estado_pago VARCHAR(20)   NOT NULL DEFAULT 'COMPLETADO',
    fecha_pago  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- añadida (el documento pide la fecha del pago)
    CONSTRAINT fk_pago_reserva FOREIGN KEY (id_reserva) REFERENCES reservas(id_reserva),
    CONSTRAINT ck_pago_monto  CHECK (monto > 0),
    CONSTRAINT ck_pago_metodo CHECK (metodo_pago IN ('TARJETA_CREDITO','TARJETA_DEBITO','TRANSFERENCIA','EFECTIVO')),
    CONSTRAINT ck_pago_estado CHECK (estado_pago IN ('PENDIENTE','COMPLETADO','RECHAZADO','REEMBOLSADO'))
) ENGINE=InnoDB;

CREATE TABLE reclamos (
    id_reclamo         BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_huesped         BIGINT      NOT NULL,
    id_reserva         BIGINT      NOT NULL,
    id_usuario_atiende BIGINT      NULL,
    tipo               VARCHAR(50) NOT NULL,
    descripcion        TEXT        NOT NULL,
    estado             VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- añadida
    CONSTRAINT fk_rec_huesped FOREIGN KEY (id_huesped)         REFERENCES huespedes(id_huesped),
    CONSTRAINT fk_rec_reserva FOREIGN KEY (id_reserva)         REFERENCES reservas(id_reserva),
    CONSTRAINT fk_rec_usuario FOREIGN KEY (id_usuario_atiende) REFERENCES usuarios(id_usuario),
    CONSTRAINT ck_rec_tipo   CHECK (tipo IN ('LIMPIEZA','FACTURACION','INFRAESTRUCTURA','ATENCION')),
    CONSTRAINT ck_rec_estado CHECK (estado IN ('PENDIENTE','EN_PROCESO','RESUELTO','RECHAZADO'))
) ENGINE=InnoDB;

-- =====================================================================
--  DATOS DE EJEMPLO
-- =====================================================================
INSERT INTO roles (nombre_rol) VALUES ('ADMIN'), ('RECEPCIONISTA'), ('CLIENTE');

INSERT INTO hoteles (nombre, ciudad, pais, categoria, direccion, telefono) VALUES
 ('Hotel Central',        'San Salvador', 'El Salvador', 4, 'Col. Escalón, Paseo General Escalón #123', '2260-0000'),
 ('Hotel Costa del Sol',  'La Libertad',  'El Salvador', 5, 'Km 65 Carretera del Litoral',             '2330-1000'),
 ('Hotel Volcán',         'Santa Ana',    'El Salvador', 3, '2a Avenida Sur, frente al parque',         '2440-2000');

INSERT INTO tipos_habitacion (nombre, capacidad_maxima) VALUES
 ('Individual', 1), ('Doble', 2), ('Familiar', 4), ('Suite', 3);

INSERT INTO servicios (nombre, categoria, precio) VALUES
 ('Desayuno buffet',            'ALIMENTACION', 8.00),
 ('Transporte al aeropuerto',   'TRANSPORTE',  25.00),
 ('Lavandería',                 'LIMPIEZA',    12.00),
 ('Acceso a spa',               'OTROS',       35.00),
 ('Estacionamiento',            'OTROS',        0.00),
 ('WiFi de alta velocidad',     'OTROS',        0.00);

INSERT INTO politicas_cancelacion (id_hotel, dias_previos_limite, porcentaje_cargo) VALUES
 (1, 2, 50.00), (2, 3, 30.00), (3, 1, 100.00);

-- tipos: 1 Individual, 2 Doble, 3 Familiar, 4 Suite
INSERT INTO habitaciones (id_hotel, id_tipo_habitacion, numero_habitacion, precio_base_noche, estado) VALUES
 (1, 1, '112',  55.00, 'DISPONIBLE'),
 (1, 2, '204',  85.00, 'DISPONIBLE'),
 (1, 2, '205',  85.00, 'DISPONIBLE'),
 (1, 4, '310', 160.00, 'DISPONIBLE'),
 (2, 3, '05',  140.00, 'DISPONIBLE'),
 (2, 2, '08',   95.00, 'DISPONIBLE'),
 (2, 1, '02',   60.00, 'DISPONIBLE'),
 (2, 4, '12',  175.00, 'DISPONIBLE'),
 (3, 2, '01',   70.00, 'DISPONIBLE'),
 (3, 3, '03',  120.00, 'DISPONIBLE'),
 (3, 1, '04',   45.00, 'DISPONIBLE'),
 (3, 4, '07',  150.00, 'MANTENIMIENTO');

-- WiFi en todas las habitaciones
INSERT INTO habitacion_servicio (id_habitacion, id_servicio, incluido)
SELECT id_habitacion, 6, TRUE FROM habitaciones;
-- Estacionamiento en el Hotel Central
INSERT INTO habitacion_servicio (id_habitacion, id_servicio, incluido)
SELECT id_habitacion, 5, TRUE FROM habitaciones WHERE id_hotel = 1;
-- Desayuno buffet en Suites y Familiares
INSERT INTO habitacion_servicio (id_habitacion, id_servicio, incluido)
SELECT h.id_habitacion, 1, TRUE FROM habitaciones h
JOIN tipos_habitacion t ON t.id_tipo_habitacion = h.id_tipo_habitacion
WHERE t.nombre IN ('Suite', 'Familiar');
