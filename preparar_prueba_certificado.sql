-- Script de preparación SOLO PARA PRUEBAS del examen (no va al repositorio)
-- 1) Recrea la base erp_little_horizons con tu DDL (sin datos)
-- 2) Carga el catálogo de grados de tu proyecto
-- 3) Carga los datos de prueba del examen (estudiantes 1, 2 y 3 del año 2025)

-- ============================================================================
-- Little Horizons ERP - Script de Base de Datos Completo (DDL + DML)
-- Sistema de Planificación de Recursos Empresariales (ERP) Educativo
-- Institución: Colegio Internacional Little Horizons S.A.S.
-- Marco Legal: MEN (Ley 115/1994, Decreto 1290/2009, Decreto 1075/2015, SIMAT)
-- Marco Tributario: Estatuto Tributario Art. 476, Facturación Electrónica DIAN
-- Motor Requerido: MySQL 8.0 o superior
-- ============================================================================

DROP DATABASE IF EXISTS erp_little_horizons;
CREATE DATABASE erp_little_horizons CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_little_horizons;

-- ============================================================================
-- SECCIÓN 1: DDL (DATA DEFINITION LANGUAGE)
-- ============================================================================

-- 1. Catálogo de Grados Académicos
CREATE TABLE grados (
    id_grado INT AUTO_INCREMENT PRIMARY KEY,
    nombre_grado VARCHAR(50) NOT NULL UNIQUE COMMENT 'Ej: Preescolar, Primero, Segundo, ... Once',
    nivel_numerico INT NOT NULL UNIQUE COMMENT 'Nivel numérico para ordenamiento (0 para preescolar, 1-11 para grados)',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Catálogo de grados académicos del colegio';

-- 2. Estudiantes
-- Justificación de cambio técnico según lineamientos SIMAT/MEN:
-- Se incorporan 'RC' (Registro Civil para menores de 7 años en Preescolar/Primaria)
-- y 'PPT' (Permiso por Protección Temporal, Decreto 216/2021) para garantizar inclusión.
CREATE TABLE estudiantes (
    id_estudiante INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento ENUM('RC', 'TI', 'CC', 'CE', 'Pasaporte', 'PPT') NOT NULL DEFAULT 'TI',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    fecha_nacimiento DATE NOT NULL,
    genero ENUM('M', 'F', 'Otro') NOT NULL,
    direccion VARCHAR(255),
    telefono VARCHAR(20),
    correo_electronico VARCHAR(100),
    tipo_sangre VARCHAR(5),
    alergias TEXT,
    condiciones_medicas TEXT,
    fecha_ingreso DATE NOT NULL,
    estado ENUM('Activo', 'Graduado', 'Retirado', 'Suspendido') DEFAULT 'Activo',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_documento (tipo_documento, numero_documento),
    INDEX idx_estado (estado),
    INDEX idx_nombre (primer_apellido, segundo_apellido, primer_nombre)
) ENGINE=InnoDB COMMENT='Información completa de estudiantes';

-- 3. Acudientes / Tutores Legales
CREATE TABLE acudientes (
    id_acudiente INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento ENUM('CC', 'CE', 'Pasaporte', 'PPT') NOT NULL DEFAULT 'CC',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    parentesco VARCHAR(50) NOT NULL COMMENT 'Padre, Madre, Abuelo, Tío, Tutor Legal, etc.',
    telefono_principal VARCHAR(20) NOT NULL,
    telefono_secundario VARCHAR(20),
    correo_electronico VARCHAR(100),
    direccion VARCHAR(255),
    ocupacion VARCHAR(100),
    es_responsable_financiero BOOLEAN DEFAULT TRUE COMMENT 'Indica si es responsable de pagos',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_documento (tipo_documento, numero_documento),
    INDEX idx_nombre (primer_apellido, segundo_apellido)
) ENGINE=InnoDB COMMENT='Acudientes y tutores legales';

-- 4. Relación Estudiantes - Acudientes (Muchos a Muchos)
CREATE TABLE estudiante_acudiente (
    id_estudiante INT NOT NULL,
    id_acudiente INT NOT NULL,
    es_principal BOOLEAN DEFAULT FALSE COMMENT 'Acudiente principal para comunicaciones',
    es_contacto_emergencia BOOLEAN DEFAULT TRUE COMMENT 'Disponible para emergencias',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_estudiante, id_acudiente),
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_acudiente) REFERENCES acudientes(id_acudiente) ON DELETE CASCADE,
    INDEX idx_estudiante (id_estudiante),
    INDEX idx_acudiente (id_acudiente)
) ENGINE=InnoDB COMMENT='Relación entre estudiantes y sus acudientes';

-- 5. Matrículas por Año y Grado
CREATE TABLE matriculas (
    id_matricula INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_grado INT NOT NULL,
    año_academico INT NOT NULL COMMENT 'Año académico (ej: 2026)',
    estado_matricula ENUM('Activa', 'Completada', 'Retirada') DEFAULT 'Activa',
    fecha_matricula DATE NOT NULL,
    fecha_retiro DATE,
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_grado) REFERENCES grados(id_grado),
    UNIQUE KEY matricula_unica (id_estudiante, año_academico),
    INDEX idx_año_grado (año_academico, id_grado),
    INDEX idx_estado (estado_matricula)
) ENGINE=InnoDB COMMENT='Historial de matrículas de estudiantes por año y grado';

-- 6. Materias / Asignaturas
CREATE TABLE materias (
    id_materia INT AUTO_INCREMENT PRIMARY KEY,
    codigo_materia VARCHAR(20) NOT NULL UNIQUE COMMENT 'Código único de la materia (ej: MAT-5, ESP-3)',
    nombre_materia VARCHAR(100) NOT NULL,
    descripcion TEXT,
    id_grado INT NOT NULL COMMENT 'Grado al que pertenece la materia',
    horas_semanales INT DEFAULT 2 COMMENT 'Horas semanales asignadas',
    esta_activa BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_grado) REFERENCES grados(id_grado),
    INDEX idx_grado (id_grado),
    INDEX idx_activa (esta_activa),
    INDEX idx_codigo (codigo_materia)
) ENGINE=InnoDB COMMENT='Catálogo de materias por grado';

-- 7. Profesores
CREATE TABLE profesores (
    id_profesor INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento ENUM('CC', 'CE', 'Pasaporte', 'PPT') NOT NULL DEFAULT 'CC',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    correo_electronico VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    especializacion VARCHAR(100) COMMENT 'Área de especialización (Matemáticas, Español, etc.)',
    fecha_contratacion DATE NOT NULL,
    estado ENUM('Activo', 'Inactivo', 'Licencia') DEFAULT 'Activo',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_documento (tipo_documento, numero_documento),
    INDEX idx_estado (estado),
    INDEX idx_nombre (primer_apellido, segundo_apellido)
) ENGINE=InnoDB COMMENT='Información de profesores';

-- 8. Asignación de Materias a Profesores
CREATE TABLE asignaciones_profesor (
    id_asignacion INT AUTO_INCREMENT PRIMARY KEY,
    id_profesor INT NOT NULL,
    id_materia INT NOT NULL,
    año_academico INT NOT NULL,
    seccion VARCHAR(10) COMMENT 'Sección o grupo (A, B, C)',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    UNIQUE KEY asignacion_unica (id_profesor, id_materia, año_academico, seccion),
    INDEX idx_profesor_año (id_profesor, año_academico),
    INDEX idx_materia_año (id_materia, año_academico)
) ENGINE=InnoDB COMMENT='Asignación de profesores a materias por año académico';

-- 9. Inscripción de Estudiantes en Materias
CREATE TABLE inscripciones_materia (
    id_inscripcion INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    año_academico INT NOT NULL,
    periodo_academico ENUM('I', 'II', 'III', 'IV') NOT NULL COMMENT 'Período académico',
    seccion VARCHAR(10) COMMENT 'Sección o grupo asignado',
    fecha_inscripcion DATE NOT NULL,
    estado ENUM('Activa', 'Retirada', 'Completada') DEFAULT 'Activa',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    UNIQUE KEY inscripcion_unica (id_estudiante, id_materia, año_academico, periodo_academico),
    INDEX idx_estudiante_año (id_estudiante, año_academico),
    INDEX idx_materia_periodo (id_materia, periodo_academico)
) ENGINE=InnoDB COMMENT='Inscripción de estudiantes en materias específicas';

-- 10. Registros Académicos (Calificaciones según estándar MEN Decreto 1290/2009)
CREATE TABLE registros_academicos (
    id_registro INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    id_profesor INT NOT NULL,
    año_academico INT NOT NULL,
    periodo_academico ENUM('I', 'II', 'III', 'IV', 'Final') NOT NULL,
    valor_nota DECIMAL(2,1) NOT NULL COMMENT 'Nota de 0.0 a 5.0 según estándar colombiano',
    observaciones TEXT,
    fecha_registro DATE NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor),
    CHECK (valor_nota >= 0.0 AND valor_nota <= 5.0),
    UNIQUE KEY registro_unico (id_estudiante, id_materia, año_academico, periodo_academico),
    INDEX idx_estudiante_año (id_estudiante, año_academico),
    INDEX idx_materia_periodo (id_materia, periodo_academico),
    INDEX idx_valor_nota (valor_nota)
) ENGINE=InnoDB COMMENT='Registro de calificaciones de estudiantes';

-- 11. Asistencias Diarias
CREATE TABLE asistencias (
    id_asistencia INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    fecha_asistencia DATE NOT NULL,
    estado_asistencia ENUM('Presente', 'Ausente', 'Tardanza', 'Excusa Médica') NOT NULL,
    observaciones TEXT,
    registrado_por INT COMMENT 'ID del profesor que registró la asistencia',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    FOREIGN KEY (registrado_por) REFERENCES profesores(id_profesor),
    UNIQUE KEY asistencia_unica (id_estudiante, id_materia, fecha_asistencia),
    INDEX idx_estudiante_fecha (id_estudiante, fecha_asistencia),
    INDEX idx_materia_fecha (id_materia, fecha_asistencia),
    INDEX idx_estado (estado_asistencia)
) ENGINE=InnoDB COMMENT='Registro de asistencia diaria de estudiantes';

-- 12. Catálogo de Clubes Extracurriculares
CREATE TABLE clubes (
    id_club INT AUTO_INCREMENT PRIMARY KEY,
    nombre_club VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    id_profesor_supervisor INT COMMENT 'Profesor encargado del club',
    capacidad_maxima INT DEFAULT 30 COMMENT 'Cupo máximo de estudiantes',
    horario_reunion VARCHAR(100) COMMENT 'Días y horario de reunión (ej: Martes 3-5 PM)',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_profesor_supervisor) REFERENCES profesores(id_profesor),
    INDEX idx_activo (esta_activo),
    INDEX idx_nombre (nombre_club)
) ENGINE=InnoDB COMMENT='Catálogo de clubes extracurriculares';

-- 13. Membresías en Clubes
CREATE TABLE membresias_club (
    id_membresia INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_club INT NOT NULL,
    fecha_ingreso DATE NOT NULL,
    estado_membresia ENUM('Activa', 'Retirada') DEFAULT 'Activa',
    fecha_retiro DATE,
    motivo_retiro TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_club) REFERENCES clubes(id_club) ON DELETE CASCADE,
    INDEX idx_estudiante (id_estudiante),
    INDEX idx_club_estado (id_club, estado_membresia)
) ENGINE=InnoDB COMMENT='Membresías de estudiantes en clubes';

-- 14. Catálogo de Eventos
CREATE TABLE eventos (
    id_evento INT AUTO_INCREMENT PRIMARY KEY,
    nombre_evento VARCHAR(150) NOT NULL,
    descripcion TEXT,
    tipo_evento ENUM('Académico', 'Cultural', 'Deportivo', 'Social', 'Científico') NOT NULL,
    fecha_evento DATE NOT NULL,
    lugar VARCHAR(150),
    tiene_competencia BOOLEAN DEFAULT FALSE COMMENT 'Indica si el evento tiene competencia con posiciones',
    id_organizador INT COMMENT 'Profesor organizador del evento',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_organizador) REFERENCES profesores(id_profesor),
    INDEX idx_fecha (fecha_evento),
    INDEX idx_tipo (tipo_evento),
    INDEX idx_activo (esta_activo)
) ENGINE=InnoDB COMMENT='Catálogo de eventos del colegio';

-- 15. Participantes en Eventos
CREATE TABLE participantes_evento (
    id_participacion INT AUTO_INCREMENT PRIMARY KEY,
    id_evento INT NOT NULL,
    id_estudiante INT NOT NULL,
    rol_participacion ENUM('Participante', 'Competidor', 'Organizador', 'Asistente') DEFAULT 'Participante',
    posicion_obtenida VARCHAR(20) COMMENT 'Posición obtenida si hay competencia (1er Lugar, 2do Lugar, 3er Lugar, Participante)',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_evento) REFERENCES eventos(id_evento) ON DELETE CASCADE,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    UNIQUE KEY participacion_unica (id_evento, id_estudiante),
    INDEX idx_evento (id_evento),
    INDEX idx_estudiante (id_estudiante)
) ENGINE=InnoDB COMMENT='Registro de participación de estudiantes en eventos';

-- 16. Conceptos de Pago (Estatuto Tributario Art. 476 Numeral 6)
CREATE TABLE conceptos_pago (
    id_concepto INT AUTO_INCREMENT PRIMARY KEY,
    codigo_concepto VARCHAR(20) NOT NULL UNIQUE,
    nombre_concepto VARCHAR(100) NOT NULL,
    descripcion TEXT,
    monto_base DECIMAL(10,2) NOT NULL COMMENT 'Valor base del concepto en pesos colombianos',
    es_recurrente BOOLEAN DEFAULT FALSE COMMENT 'Indica si es un pago recurrente (ej: pensión mensual)',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_activo (esta_activo),
    INDEX idx_codigo (codigo_concepto)
) ENGINE=InnoDB COMMENT='Catálogo de conceptos de pago del colegio';

-- 17. Registro de Pagos
CREATE TABLE pagos (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_acudiente INT COMMENT 'Acudiente que realizó el pago',
    id_concepto INT NOT NULL,
    monto_pagado DECIMAL(10,2) NOT NULL,
    fecha_pago DATE NOT NULL,
    metodo_pago ENUM('Efectivo', 'Transferencia', 'Tarjeta Débito', 'Tarjeta Crédito', 'Cheque') NOT NULL,
    numero_recibo VARCHAR(50) UNIQUE,
    año_academico INT COMMENT 'Año al que aplica el pago',
    periodo_academico ENUM('I', 'II', 'III', 'IV', 'Anual') COMMENT 'Período al que aplica el pago',
    notas TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_acudiente) REFERENCES acudientes(id_acudiente),
    FOREIGN KEY (id_concepto) REFERENCES conceptos_pago(id_concepto),
    INDEX idx_estudiante_fecha (id_estudiante, fecha_pago),
    INDEX idx_concepto (id_concepto),
    INDEX idx_fecha (fecha_pago),
    INDEX idx_recibo (numero_recibo)
) ENGINE=InnoDB COMMENT='Registro de todos los pagos realizados';

-- 18. Facturas Electrónicas (Cumplimiento DIAN / Estatuto Tributario)
CREATE TABLE facturas (
    id_factura INT AUTO_INCREMENT PRIMARY KEY,
    numero_factura VARCHAR(50) NOT NULL UNIQUE COMMENT 'Número consecutivo de factura (ej: FE-000101)',
    id_pago INT NOT NULL COMMENT 'Pago asociado a esta factura',
    fecha_emision DATE NOT NULL,
    cufe VARCHAR(100) COMMENT 'Código Único de Factura Electrónica (SHA-384 DIAN)',
    nombre_archivo_pdf VARCHAR(255) COMMENT 'Nombre del archivo PDF generado',
    ruta_archivo VARCHAR(500) COMMENT 'Ruta completa donde se guardó el archivo',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_pago) REFERENCES pagos(id_pago) ON DELETE CASCADE,
    INDEX idx_numero_factura (numero_factura),
    INDEX idx_fecha_emision (fecha_emision),
    INDEX idx_pago (id_pago)
) ENGINE=InnoDB COMMENT='Registro de facturas electrónicas generadas';

-- 19. Categorías de Recursos
CREATE TABLE categorias_recurso (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre_categoria VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Categorías de recursos (Libro, Equipo de Laboratorio, etc.)';

-- 20. Recursos de Biblioteca, Laboratorios y Deportes
CREATE TABLE recursos (
    id_recurso INT AUTO_INCREMENT PRIMARY KEY,
    codigo_recurso VARCHAR(50) NOT NULL UNIQUE COMMENT 'Código de inventario único',
    nombre_recurso VARCHAR(150) NOT NULL,
    id_categoria INT NOT NULL,
    descripcion TEXT,
    fecha_adquisicion DATE,
    ubicacion_fisica VARCHAR(100) COMMENT 'Ubicación física (Biblioteca, Laboratorio 1, etc.)',
    estado_recurso ENUM('Disponible', 'Prestado', 'En Mantenimiento', 'Dado de Baja') DEFAULT 'Disponible',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_categoria) REFERENCES categorias_recurso(id_categoria),
    INDEX idx_codigo (codigo_recurso),
    INDEX idx_estado (estado_recurso),
    INDEX idx_categoria (id_categoria),
    INDEX idx_nombre (nombre_recurso)
) ENGINE=InnoDB COMMENT='Inventario de recursos prestables del colegio';

-- 21. Préstamos a Estudiantes
CREATE TABLE prestamos_estudiante (
    id_prestamo INT AUTO_INCREMENT PRIMARY KEY,
    id_recurso INT NOT NULL,
    id_estudiante INT NOT NULL COMMENT 'Estudiante que toma prestado el recurso',
    fecha_prestamo DATE NOT NULL,
    fecha_devolucion_esperada DATE NOT NULL,
    fecha_devolucion_real DATE,
    estado_prestamo ENUM('Activo', 'Devuelto', 'Vencido') DEFAULT 'Activo',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_recurso) REFERENCES recursos(id_recurso) ON DELETE CASCADE,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    INDEX idx_recurso_estado (id_recurso, estado_prestamo),
    INDEX idx_estudiante (id_estudiante),
    INDEX idx_estado (estado_prestamo),
    INDEX idx_fecha_devolucion (fecha_devolucion_esperada)
) ENGINE=InnoDB COMMENT='Registro de préstamos de recursos a estudiantes';

-- 22. Préstamos a Profesores
CREATE TABLE prestamos_profesor (
    id_prestamo INT AUTO_INCREMENT PRIMARY KEY,
    id_recurso INT NOT NULL,
    id_profesor INT NOT NULL COMMENT 'Profesor que toma prestado el recurso',
    fecha_prestamo DATE NOT NULL,
    fecha_devolucion_esperada DATE NOT NULL,
    fecha_devolucion_real DATE,
    estado_prestamo ENUM('Activo', 'Devuelto', 'Vencido') DEFAULT 'Activo',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_recurso) REFERENCES recursos(id_recurso) ON DELETE CASCADE,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor) ON DELETE CASCADE,
    INDEX idx_recurso_estado (id_recurso, estado_prestamo),
    INDEX idx_profesor (id_profesor),
    INDEX idx_estado (estado_prestamo),
    INDEX idx_fecha_devolucion (fecha_devolucion_esperada)
) ENGINE=InnoDB COMMENT='Registro de préstamos de recursos a profesores';



INSERT INTO grados (id_grado, nombre_grado, nivel_numerico) VALUES
(1, 'Preescolar', 0),
(2, 'Primero', 1),
(3, 'Segundo', 2),
(4, 'Tercero', 3),
(5, 'Cuarto', 4),
(6, 'Quinto', 5),
(7, 'Sexto', 6),
(8, 'Séptimo', 7),
(9, 'Octavo', 8),
(10, 'Noveno', 9),
(11, 'Décimo', 10),
(12, 'Once', 11);

-- ============================================
-- SCRIPT DE DATOS DE PRUEBA PARA EXAMEN #3
-- Generador de Certificados de Notas 2025
-- ============================================
-- Este script inserta datos de prueba para 3 estudiantes
-- con registros académicos del año 2025 en 7 materias
-- con notas de 3 períodos académicos cursados
-- ============================================
USE erp_little_horizons;
-- ============================================
-- 1. INSERTAR ESTUDIANTES DE PRUEBA
-- ============================================
INSERT INTO estudiantes (
    id_estudiante,
    tipo_documento,
    numero_documento,
    primer_nombre,
    segundo_nombre,
    primer_apellido,
    segundo_apellido,
    fecha_nacimiento,
    genero,
    fecha_ingreso,
    estado
) VALUES
(1, 'TI', '1023456789', 'Juan', 'Carlos', 'Pérez', 'González', '2010-03-15', 'M', '2022-01-15', 'Activo'),
(2, 'TI', '1023456790', 'María', 'Elena', 'López', 'Ramírez', '2010-07-22', 'F', '2022-01-15', 'Activo'),
(3, 'TI', '1023456791', 'Carlos', NULL, 'Gómez', 'Silva', '2010-11-05', 'M', '2022-01-15', 'Activo');
-- ============================================
-- 2. INSERTAR MATERIAS PARA GRADO DÉCIMO
-- ============================================

INSERT INTO materias (
    id_materia,
    codigo_materia,
    nombre_materia,
    descripcion,
    id_grado,
    horas_semanales,
    esta_activa
) VALUES
(1, 'MAT-10', 'Matemáticas', 'Matemáticas para grado décimo', 10, 5, TRUE),
(2, 'FIS-10', 'Física', 'Física para grado décimo', 10, 4, TRUE),
(3, 'QUI-10', 'Química', 'Química para grado décimo', 10, 4, TRUE),
(4, 'ING-10', 'Inglés', 'Inglés avanzado para grado décimo', 10, 3, TRUE),
(5, 'ESP-10', 'Español', 'Lengua castellana para grado décimo', 10, 4, TRUE),
(6, 'SOC-10', 'Ciencias Sociales', 'Ciencias sociales para grado décimo', 10, 3, TRUE),
(7, 'BIO-10', 'Biología', 'Biología para grado décimo', 10, 4, TRUE);
-- ============================================
-- 3. INSERTAR PROFESOR DE PRUEBA
-- ============================================
INSERT INTO profesores (
    id_profesor,
    tipo_documento,
    numero_documento,
    primer_nombre,
    segundo_nombre,
    primer_apellido,
    segundo_apellido,
    correo_electronico,
    telefono,
    especializacion,
    fecha_contratacion,
    estado
) VALUES
(1, 'CC', '80123456', 'Roberto', 'Andrés', 'Martínez', 'Torres', 'rmartinez@littlehorizons.edu', '3001234567', 'Matemáticas', '2020-01-15', 'Activo');
-- ============================================
-- 4. INSERTAR MATRÍCULAS AÑO 2025
-- ============================================
INSERT INTO matriculas (
    id_matricula,
    id_estudiante,
    id_grado,
    año_academico,
    estado_matricula,
    fecha_matricula
) VALUES
(1, 1, 10, 2025, 'Activa', '2025-01-15'),
(2, 2, 10, 2025, 'Activa', '2025-01-15'),
(3, 3, 10, 2025, 'Activa', '2025-01-15');
-- ============================================
-- 5. INSERTAR REGISTROS ACADÉMICOS - ESTUDIANTE 1
--    Juan Carlos Pérez González
--    3 períodos cursados en 7 materias
-- ============================================
-- Matemáticas (Per I: 4.8, Per II: 4.5, Per III: 4.7)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(1, 1, 1, 2025, 'I', 4.8, '2025-03-15'),
(1, 1, 1, 2025, 'II', 4.5, '2025-06-15'),
(1, 1, 1, 2025, 'III', 4.7, '2025-09-15');
-- Física (Per I: 3.5, Per II: 3.8, Per III: 4.0)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(1, 2, 1, 2025, 'I', 3.5, '2025-03-15'),
(1, 2, 1, 2025, 'II', 3.8, '2025-06-15'),
(1, 2, 1, 2025, 'III', 4.0, '2025-09-15');
-- Química (Per I: 4.2, Per II: 4.0, Per III: 4.5)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(1, 3, 1, 2025, 'I', 4.2, '2025-03-15'),
(1, 3, 1, 2025, 'II', 4.0, '2025-06-15'),
(1, 3, 1, 2025, 'III', 4.5, '2025-09-15');
-- Inglés (Per I: 5.0, Per II: 4.8, Per III: 4.9)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(1, 4, 1, 2025, 'I', 5.0, '2025-03-15'),
(1, 4, 1, 2025, 'II', 4.8, '2025-06-15'),
(1, 4, 1, 2025, 'III', 4.9, '2025-09-15');
-- Español (Per I: 4.3, Per II: 4.5, Per III: 4.4)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(1, 5, 1, 2025, 'I', 4.3, '2025-03-15'),
(1, 5, 1, 2025, 'II', 4.5, '2025-06-15'),
(1, 5, 1, 2025, 'III', 4.4, '2025-09-15');
-- Ciencias Sociales (Per I: 4.0, Per II: 4.2, Per III: 4.1)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(1, 6, 1, 2025, 'I', 4.0, '2025-03-15'),
(1, 6, 1, 2025, 'II', 4.2, '2025-06-15'),
(1, 6, 1, 2025, 'III', 4.1, '2025-09-15');
-- Biología (Per I: 3.8, Per II: 4.0, Per III: 4.2)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(1, 7, 1, 2025, 'I', 3.8, '2025-03-15'),
(1, 7, 1, 2025, 'II', 4.0, '2025-06-15'),
(1, 7, 1, 2025, 'III', 4.2, '2025-09-15');
-- ============================================
-- 6. INSERTAR REGISTROS ACADÉMICOS - ESTUDIANTE 2
--    María Elena López Ramírez
--    3 períodos cursados en 7 materias
-- ============================================
-- Matemáticas (Per I: 3.5, Per II: 3.8, Per III: 4.0)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(2, 1, 1, 2025, 'I', 3.5, '2025-03-15'),
(2, 1, 1, 2025, 'II', 3.8, '2025-06-15'),
(2, 1, 1, 2025, 'III', 4.0, '2025-09-15');
-- Física (Per I: 3.2, Per II: 3.5, Per III: 3.7)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(2, 2, 1, 2025, 'I', 3.2, '2025-03-15'),
(2, 2, 1, 2025, 'II', 3.5, '2025-06-15'),
(2, 2, 1, 2025, 'III', 3.7, '2025-09-15');
-- Química (Per I: 4.5, Per II: 4.7, Per III: 4.8)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(2, 3, 1, 2025, 'I', 4.5, '2025-03-15'),
(2, 3, 1, 2025, 'II', 4.7, '2025-06-15'),
(2, 3, 1, 2025, 'III', 4.8, '2025-09-15');
-- Inglés (Per I: 4.8, Per II: 4.9, Per III: 5.0)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(2, 4, 1, 2025, 'I', 4.8, '2025-03-15'),
(2, 4, 1, 2025, 'II', 4.9, '2025-06-15'),
(2, 4, 1, 2025, 'III', 5.0, '2025-09-15');
-- Español (Per I: 4.6, Per II: 4.7, Per III: 4.5)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(2, 5, 1, 2025, 'I', 4.6, '2025-03-15'),
(2, 5, 1, 2025, 'II', 4.7, '2025-06-15'),
(2, 5, 1, 2025, 'III', 4.5, '2025-09-15');
-- Ciencias Sociales (Per I: 4.2, Per II: 4.3, Per III: 4.4)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(2, 6, 1, 2025, 'I', 4.2, '2025-03-15'),
(2, 6, 1, 2025, 'II', 4.3, '2025-06-15'),
(2, 6, 1, 2025, 'III', 4.4, '2025-09-15');
-- Biología (Per I: 4.0, Per II: 4.1, Per III: 4.3)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(2, 7, 1, 2025, 'I', 4.0, '2025-03-15'),
(2, 7, 1, 2025, 'II', 4.1, '2025-06-15'),
(2, 7, 1, 2025, 'III', 4.3, '2025-09-15');
-- ============================================
-- 7. INSERTAR REGISTROS ACADÉMICOS - ESTUDIANTE 3
--    Carlos Gómez Silva (sin segundo nombre)
--    3 períodos cursados en 7 materias
-- ============================================
-- Matemáticas (Per I: 4.0, Per II: 4.2, Per III: 4.3)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(3, 1, 1, 2025, 'I', 4.0, '2025-03-15'),
(3, 1, 1, 2025, 'II', 4.2, '2025-06-15'),
(3, 1, 1, 2025, 'III', 4.3, '2025-09-15');
-- Física (Per I: 4.5, Per II: 4.6, Per III: 4.7)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(3, 2, 1, 2025, 'I', 4.5, '2025-03-15'),
(3, 2, 1, 2025, 'II', 4.6, '2025-06-15'),
(3, 2, 1, 2025, 'III', 4.7, '2025-09-15');
-- Química (Per I: 3.8, Per II: 3.9, Per III: 4.0)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(3, 3, 1, 2025, 'I', 3.8, '2025-03-15'),
(3, 3, 1, 2025, 'II', 3.9, '2025-06-15'),
(3, 3, 1, 2025, 'III', 4.0, '2025-09-15');
-- Inglés (Per I: 3.5, Per II: 3.7, Per III: 3.8)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(3, 4, 1, 2025, 'I', 3.5, '2025-03-15'),
(3, 4, 1, 2025, 'II', 3.7, '2025-06-15'),
(3, 4, 1, 2025, 'III', 3.8, '2025-09-15');
-- Español (Per I: 4.1, Per II: 4.3, Per III: 4.2)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(3, 5, 1, 2025, 'I', 4.1, '2025-03-15'),
(3, 5, 1, 2025, 'II', 4.3, '2025-06-15'),
(3, 5, 1, 2025, 'III', 4.2, '2025-09-15');
-- Ciencias Sociales (Per I: 3.9, Per II: 4.0, Per III: 4.1)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(3, 6, 1, 2025, 'I', 3.9, '2025-03-15'),
(3, 6, 1, 2025, 'II', 4.0, '2025-06-15'),
(3, 6, 1, 2025, 'III', 4.1, '2025-09-15');
-- Biología (Per I: 4.4, Per II: 4.5, Per III: 4.6)
INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, fecha_registro) VALUES
(3, 7, 1, 2025, 'I', 4.4, '2025-03-15'),
(3, 7, 1, 2025, 'II', 4.5, '2025-06-15'),
(3, 7, 1, 2025, 'III', 4.6, '2025-09-15');
-- ============================================
-- FIN DEL SCRIPT DE DATOS DE PRUEBA
-- ============================================
