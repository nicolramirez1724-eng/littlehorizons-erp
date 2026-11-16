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


-- ============================================================================
-- SECCIÓN 2: DML (DATA MANIPULATION LANGUAGE)
-- Datos de prueba realistas, consistentes con el contexto colombiano.
-- Mínimo 30 registros en cada tabla principal requerida.
-- ============================================================================

-- 1. Grados Académicos
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

-- 2. Categorías de Recursos
INSERT INTO categorias_recurso (id_categoria, nombre_categoria, descripcion) VALUES
(1, 'Libro', 'Textos escolares, novelas, enciclopedias y literatura'),
(2, 'Equipo de Laboratorio', 'Microscopios, tubos de ensayo, mecheros Bunsen, balanzas analíticas'),
(3, 'Equipo Deportivo', 'Balones de fútbol, baloncesto, voleibol, colchonetas y conos'),
(4, 'Dispositivo Tecnológico', 'Tablets educativas, portátiles para laboratorios, calculadoras científicas'),
(5, 'Material Didáctico', 'Mapas mundi, globos terráqueos, modelos anatómicos y juegos de ajedrez');

-- 3. Conceptos de Pago (Marco Tributario Estatuto Tributario Art. 476 Numeral 6)
INSERT INTO conceptos_pago (id_concepto, codigo_concepto, nombre_concepto, descripcion, monto_base, es_recurrente, esta_activo) VALUES
(1, 'MAT', 'Matrícula Anual', 'Derecho de matrícula anual reglamentaria (Excluido IVA Art. 476 ET)', 550000.00, FALSE, TRUE),
(2, 'PENSION', 'Pensión Mensual', 'Cuota mensual de enseñanza escolar (Excluido IVA Art. 476 ET)', 380000.00, TRUE, TRUE),
(3, 'UNIF_DIA', 'Uniforme de Diario', 'Uniforme de diario institucional completo', 220000.00, FALSE, TRUE),
(4, 'UNIF_DEP', 'Uniforme de Educación Física', 'Sudadera institucional y camiseta deportiva', 180000.00, FALSE, TRUE),
(5, 'TRANSPORTE', 'Ruta Escolar Mensual', 'Servicio de transporte puerta a puerta mensual', 170000.00, TRUE, TRUE),
(6, 'ALIMENTACION', 'Restaurante Escolar', 'Almuerzo balanceado y refrigerio mensual', 160000.00, TRUE, TRUE),
(7, 'SALIDA_PED', 'Salida Pedagógica', 'Visita guiada a museos, parques naturales o granjas', 65000.00, FALSE, TRUE),
(8, 'CONST_EST', 'Constancia y Certificados', 'Emisión de certificados de estudio oficiales', 18000.00, FALSE, TRUE),
(9, 'SEGURO_EST', 'Póliza Seguro Estudiantil', 'Seguro de accidentes escolares con cobertura anual', 45000.00, FALSE, TRUE),
(10, 'CLUB_ROBOT', 'Inscripción Club Robótica', 'Materiales y kits para el club extracurricular', 90000.00, FALSE, TRUE);

-- 4. Acudientes (30 registros)
INSERT INTO acudientes (id_acudiente, tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, parentesco, telefono_principal, telefono_secundario, correo_electronico, direccion, ocupacion, es_responsable_financiero) VALUES
(1, 'CC', '79845123', 'Carlos', 'Alberto', 'Gomez', 'Perez', 'Padre', '3102345678', '6013456789', 'carlos.gomez@gmail.com', 'Calle 127 # 15-45, Bogotá', 'Ingeniero Civil', TRUE),
(2, 'CC', '52345678', 'Maria', 'Fernanda', 'Rodriguez', 'Lopez', 'Madre', '3113456789', '6014567890', 'mf.rodriguez@outlook.com', 'Calle 127 # 15-45, Bogotá', 'Arquitecta', TRUE),
(3, 'CC', '80123456', 'Juan', 'David', 'Martinez', 'Soto', 'Padre', '3124567890', NULL, 'juan.martinez@soto.com', 'Carrera 7 # 140-20, Bogotá', 'Abogado', TRUE),
(4, 'CC', '53456789', 'Claudia', 'Patricia', 'Hernandez', 'Castro', 'Madre', '3135678901', '6015678901', 'claudia.hernandez@yahoo.es', 'Carrera 15 # 85-30, Bogotá', 'Médica Pediatra', TRUE),
(5, 'CC', '19234567', 'Luis', 'Eduardo', 'Gonzalez', 'Mendoza', 'Padre', '3146789012', NULL, 'luis.gonzalez@corp.co', 'Calle 100 # 19-61, Bogotá', 'Contador Público', TRUE),
(6, 'CC', '51987654', 'Adriana', 'Lucia', 'Vargas', 'Rios', 'Madre', '3157890123', '6017890123', 'adriana.vargas@hotmail.com', 'Avenida Boyacá # 138-12, Bogotá', 'Docente Universitaria', TRUE),
(7, 'CE', '458921', 'Jean', 'Paul', 'Dubois', 'Moreau', 'Padre', '3168901234', NULL, 'jean.dubois@frenchtech.fr', 'Carrera 9 # 72-35, Bogotá', 'Diplomático', TRUE),
(8, 'CC', '52789123', 'Gloria', 'Esperanza', 'Ramirez', 'Diaz', 'Madre', '3179012345', '6018901234', 'gloria.ramirez@gmail.com', 'Calle 134 # 9-45, Bogotá', 'Empresaria', TRUE),
(9, 'CC', '79654321', 'Jorge', 'Enrique', 'Torres', 'Navarro', 'Padre', '3180123456', NULL, 'jorge.torres@gmail.com', 'Carrera 19 # 104-50, Bogotá', 'Economista', TRUE),
(10, 'CC', '41987321', 'Luz', 'Marina', 'Morales', 'Jimenez', 'Madre', '3191234567', '6019012345', 'luz.morales@sanitas.com.co', 'Calle 80 # 68-90, Bogotá', 'Enfermera Jefe', TRUE),
(11, 'CC', '19876543', 'Andres', 'Felipe', 'Castro', 'Ortega', 'Padre', '3103456789', NULL, 'andres.castro@gmail.com', 'Transversal 23 # 95-10, Bogotá', 'Administrador', TRUE),
(12, 'CC', '52876543', 'Sonia', 'Rocio', 'Salazar', 'Mejia', 'Madre', '3114567890', '6012345678', 'sonia.salazar@banco.com.co', 'Carrera 58 # 127-15, Bogotá', 'Analista Financiera', TRUE),
(13, 'PPT', '98451234', 'Jose', 'Manuel', 'Silva', 'Briceno', 'Padre', '3125678901', NULL, 'jose.silva@gmail.com', 'Calle 68 # 50-22, Bogotá', 'Técnico Eléctrico', TRUE),
(14, 'CC', '51234567', 'Martha', 'Cecilia', 'Rojas', 'Guerrero', 'Madre', '3136789012', '6013456789', 'martha.rojas@gmail.com', 'Calle 147 # 19-30, Bogotá', 'Diseñadora Gráfica', TRUE),
(15, 'CC', '80234567', 'Gustavo', 'Adolfo', 'Paredes', 'Vargas', 'Padre', '3147890123', NULL, 'gustavo.paredes@gmail.com', 'Carrera 11 # 98-40, Bogotá', 'Consultor TI', TRUE),
(16, 'CC', '52456789', 'Liliana', 'Patricia', 'Cortes', 'Suarez', 'Madre', '3158901234', '6014567890', 'liliana.cortes@gmail.com', 'Calle 116 # 15-28, Bogotá', 'Psicóloga', TRUE),
(17, 'CC', '79123456', 'Mauricio', 'Javier', 'Guzman', 'Herrera', 'Padre', '3169012345', NULL, 'mauricio.guzman@gmail.com', 'Carrera 7 # 113-50, Bogotá', 'Ingeniero de Sistemas', TRUE),
(18, 'CC', '52654321', 'Diana', 'Carolina', 'Alvarez', 'Pinzon', 'Madre', '3170123456', '6015678901', 'diana.alvarez@gmail.com', 'Calle 152 # 20-45, Bogotá', 'Comunicadora Social', TRUE),
(19, 'CC', '19456789', 'Fabio', 'Hernan', 'Reyes', 'Camargo', 'Padre', '3181234567', NULL, 'fabio.reyes@gmail.com', 'Carrera 15 # 124-10, Bogotá', 'Comerciante', TRUE),
(20, 'CC', '51789456', 'Paola', 'Andrea', 'Cardona', 'Velasquez', 'Madre', '3192345678', '6016789012', 'paola.cardona@gmail.com', 'Calle 122 # 18-35, Bogotá', 'Odontóloga', TRUE),
(21, 'CC', '80345678', 'Hector', 'Fabio', 'Valencia', 'Giraldo', 'Padre', '3104567890', NULL, 'hector.valencia@gmail.com', 'Carrera 9 # 100-02, Bogotá', 'Químico Farmacéutico', TRUE),
(22, 'CC', '52987123', 'Sandra', 'Milena', 'Restrepo', 'Zapata', 'Madre', '3115678901', '6017890123', 'sandra.restrepo@gmail.com', 'Calle 170 # 8-30, Bogotá', 'Bióloga', TRUE),
(23, 'CC', '79345678', 'Alvaro', 'Jose', 'Medina', 'Rincon', 'Padre', '3126789012', NULL, 'alvaro.medina@gmail.com', 'Carrera 45 # 108-20, Bogotá', 'Abogado Penalista', TRUE),
(24, 'CC', '52123987', 'Esperanza', 'Ines', 'Cardenas', 'Luna', 'Madre', '3137890123', '6018901234', 'esperanza.cardenas@gmail.com', 'Calle 138 # 55-12, Bogotá', 'Nutricionista', TRUE),
(25, 'CC', '80456789', 'Rafael', 'Antonio', 'Bermudez', 'Ochoa', 'Padre', '3148901234', NULL, 'rafael.bermudez@gmail.com', 'Carrera 13 # 94-44, Bogotá', 'Piloto Comercial', TRUE),
(26, 'CC', '53012345', 'Marcela', 'Viviana', 'Montoya', 'Castaño', 'Madre', '3159012345', '6019012345', 'marcela.montoya@gmail.com', 'Calle 161 # 14-22, Bogotá', 'Gerente de Mercadeo', TRUE),
(27, 'CC', '79567890', 'Fernando', 'Alonso', 'Villamizar', 'Duque', 'Padre', '3160123456', NULL, 'fernando.villamizar@gmail.com', 'Carrera 7 # 156-68, Bogotá', 'Ingeniero Mecánico', TRUE),
(28, 'CC', '52321654', 'Consuelo', 'Maria', 'Arango', 'Henao', 'Madre', '3171234567', '6012345678', 'consuelo.arango@gmail.com', 'Calle 140 # 11-18, Bogotá', 'Veterinaria', TRUE),
(29, 'CC', '19654321', 'Oscar', 'Ivan', 'Zamora', 'Pardo', 'Padre', '3182345678', NULL, 'oscar.zamora@gmail.com', 'Carrera 18 # 82-55, Bogotá', 'Corredor de Bolsa', TRUE),
(30, 'CC', '51654987', 'Nidia', 'Stella', 'Parra', 'Guevara', 'Madre', '3193456789', '6013456789', 'nidia.parra@gmail.com', 'Calle 106 # 14-60, Bogotá', 'Secretaria Ejecutiva', TRUE);

-- 5. Estudiantes (32 registros con tipos SIMAT: RC, TI, CC, CE, PPT)
INSERT INTO estudiantes (id_estudiante, tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, correo_electronico, tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado) VALUES
(1, 'RC', '1025894123', 'Mateo', 'David', 'Gomez', 'Rodriguez', '2021-03-15', 'M', 'Calle 127 # 15-45, Bogotá', '3102345678', 'est.mateo.gomez@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2026-01-20', 'Activo'),
(2, 'RC', '1025894124', 'Sofia', 'Isabella', 'Gomez', 'Rodriguez', '2020-05-18', 'F', 'Calle 127 # 15-45, Bogotá', '3102345678', 'est.sofia.gomez@littlehorizons.edu.co', 'O+', 'Polen', 'Rinitis alérgica', '2025-01-20', 'Activo'),
(3, 'RC', '1036789123', 'Santiago', 'Andres', 'Martinez', 'Hernandez', '2019-08-10', 'M', 'Carrera 7 # 140-20, Bogotá', '3124567890', 'est.santiago.martinez@littlehorizons.edu.co', 'A+', 'Penicilina', 'Ninguna', '2024-01-22', 'Activo'),
(4, 'TI', '1098765432', 'Valentina', 'Rocio', 'Gonzalez', 'Vargas', '2017-02-14', 'F', 'Calle 100 # 19-61, Bogotá', '3146789012', 'est.valentina.gonzalez@littlehorizons.edu.co', 'B+', 'Ninguna', 'Ninguna', '2023-01-25', 'Activo'),
(5, 'TI', '1098765433', 'Nicolas', 'Eduardo', 'Gonzalez', 'Vargas', '2015-11-20', 'M', 'Calle 100 # 19-61, Bogotá', '3146789012', 'est.nicolas.gonzalez@littlehorizons.edu.co', 'B+', 'Maní', 'Asma leve', '2021-01-25', 'Activo'),
(6, 'CE', '9876543', 'Lucas', 'Alexandre', 'Dubois', 'Ramirez', '2016-07-04', 'M', 'Carrera 9 # 72-35, Bogotá', '3168901234', 'est.lucas.dubois@littlehorizons.edu.co', 'O-', 'Ninguna', 'Ninguna', '2022-01-24', 'Activo'),
(7, 'TI', '1087654321', 'Mariana', 'Lucia', 'Torres', 'Morales', '2014-04-12', 'F', 'Carrera 19 # 104-50, Bogotá', '3180123456', 'est.mariana.torres@littlehorizons.edu.co', 'AB+', 'Ninguna', 'Ninguna', '2020-01-27', 'Activo'),
(8, 'TI', '1076543210', 'Samuel', 'Felipe', 'Castro', 'Salazar', '2013-09-08', 'M', 'Transversal 23 # 95-10, Bogotá', '3103456789', 'est.samuel.castro@littlehorizons.edu.co', 'O+', 'Ibuprofeno', 'Miopía', '2019-01-28', 'Activo'),
(9, 'PPT', '87654321', 'Gabriel', 'Alejandro', 'Silva', 'Rojas', '2012-12-03', 'M', 'Calle 68 # 50-22, Bogotá', '3125678901', 'est.gabriel.silva@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2022-08-15', 'Activo'),
(10, 'TI', '1065432109', 'Isabella', 'Maria', 'Paredes', 'Cortes', '2011-06-25', 'F', 'Carrera 11 # 98-40, Bogotá', '3147890123', 'est.isabella.paredes@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2018-01-29', 'Activo'),
(11, 'TI', '1054321098', 'Daniel', 'Esteban', 'Guzman', 'Alvarez', '2010-01-30', 'M', 'Carrera 7 # 113-50, Bogotá', '3169012345', 'est.daniel.guzman@littlehorizons.edu.co', 'A-', 'Polvo', 'Ninguna', '2017-01-30', 'Activo'),
(12, 'TI', '1043210987', 'Camila', 'Andrea', 'Reyes', 'Cardona', '2009-10-15', 'F', 'Carrera 15 # 124-10, Bogotá', '3181234567', 'est.camila.reyes@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2016-01-25', 'Activo'),
(13, 'TI', '1032109876', 'Alejandro', 'Jose', 'Valencia', 'Restrepo', '2008-05-22', 'M', 'Carrera 9 # 100-02, Bogotá', '3104567890', 'est.alejandro.valencia@littlehorizons.edu.co', 'B+', 'Ninguna', 'Ninguna', '2015-01-26', 'Activo'),
(14, 'CC', '1001234567', 'Sebastian', 'David', 'Medina', 'Cardenas', '2007-01-19', 'M', 'Carrera 45 # 108-20, Bogotá', '3126789012', 'est.sebastian.medina@littlehorizons.edu.co', 'O+', 'Sulfas', 'Ninguna', '2014-01-27', 'Activo'),
(15, 'CC', '1002345678', 'Valeria', 'Sofia', 'Bermudez', 'Montoya', '2007-08-05', 'F', 'Carrera 13 # 94-44, Bogotá', '3148901234', 'est.valeria.bermudez@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2014-01-27', 'Activo'),
(16, 'TI', '1012345678', 'Julian', 'Andres', 'Villamizar', 'Arango', '2008-11-11', 'M', 'Carrera 7 # 156-68, Bogotá', '3160123456', 'est.julian.villamizar@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2015-01-26', 'Activo'),
(17, 'TI', '1023456789', 'Luciana', 'Camila', 'Zamora', 'Parra', '2009-03-09', 'F', 'Carrera 18 # 82-55, Bogotá', '3182345678', 'est.luciana.zamora@littlehorizons.edu.co', 'O+', 'Picaduras abeja', 'Alergia severa', '2016-01-25', 'Activo'),
(18, 'RC', '1045678901', 'Joaquin', 'Emilio', 'Gomez', 'Rodriguez', '2022-09-01', 'M', 'Calle 127 # 15-45, Bogotá', '3102345678', 'est.joaquin.gomez@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2026-01-20', 'Activo'),
(19, 'TI', '1056789012', 'Martina', 'Lucia', 'Martinez', 'Hernandez', '2016-04-18', 'F', 'Carrera 7 # 140-20, Bogotá', '3124567890', 'est.martina.martinez@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2022-01-24', 'Activo'),
(20, 'TI', '1067890123', 'Simon', 'Pedro', 'Gonzalez', 'Vargas', '2013-08-27', 'M', 'Calle 100 # 19-61, Bogotá', '3146789012', 'est.simon.gonzalez@littlehorizons.edu.co', 'B+', 'Ninguna', 'Ninguna', '2019-01-28', 'Activo'),
(21, 'TI', '1078901234', 'Salome', 'Victoria', 'Torres', 'Morales', '2017-10-31', 'F', 'Carrera 19 # 104-50, Bogotá', '3180123456', 'est.salome.torres@littlehorizons.edu.co', 'AB+', 'Ninguna', 'Ninguna', '2023-01-25', 'Activo'),
(22, 'TI', '1089012345', 'Emmanuel', 'David', 'Castro', 'Salazar', '2015-05-05', 'M', 'Transversal 23 # 95-10, Bogotá', '3103456789', 'est.emmanuel.castro@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2021-01-25', 'Activo'),
(23, 'PPT', '76543210', 'Daniela', 'Coromoto', 'Silva', 'Rojas', '2014-09-17', 'F', 'Calle 68 # 50-22, Bogotá', '3125678901', 'est.daniela.silva@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2022-08-15', 'Activo'),
(24, 'TI', '1090123456', 'Tomas', 'Felipe', 'Paredes', 'Cortes', '2013-03-14', 'M', 'Carrera 11 # 98-40, Bogotá', '3147890123', 'est.tomas.paredes@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2019-01-28', 'Activo'),
(25, 'TI', '1009876543', 'Antonia', 'Isabel', 'Guzman', 'Alvarez', '2012-07-21', 'F', 'Carrera 7 # 113-50, Bogotá', '3169012345', 'est.antonia.guzman@littlehorizons.edu.co', 'A-', 'Ninguna', 'Ninguna', '2018-01-29', 'Activo'),
(26, 'TI', '1098761234', 'Maximiliano', 'Leon', 'Reyes', 'Cardona', '2011-12-12', 'M', 'Carrera 15 # 124-10, Bogotá', '3181234567', 'est.maximiliano.reyes@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2017-01-30', 'Activo'),
(27, 'TI', '1087659876', 'Elena', 'Beatriz', 'Valencia', 'Restrepo', '2010-09-09', 'F', 'Carrera 9 # 100-02, Bogotá', '3104567890', 'est.elena.valencia@littlehorizons.edu.co', 'B+', 'Ninguna', 'Ninguna', '2016-01-25', 'Activo'),
(28, 'CC', '1003456789', 'Cristian', 'Camilo', 'Medina', 'Cardenas', '2006-11-30', 'M', 'Carrera 45 # 108-20, Bogotá', '3126789012', 'est.cristian.medina@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2013-01-28', 'Graduado'),
(29, 'TI', '1076541234', 'Paulina', 'Ines', 'Bermudez', 'Montoya', '2009-02-17', 'F', 'Carrera 13 # 94-44, Bogotá', '3148901234', 'est.paulina.bermudez@littlehorizons.edu.co', 'A+', 'Ninguna', 'Ninguna', '2015-01-26', 'Activo'),
(30, 'TI', '1065438765', 'Felipe', 'Santiago', 'Villamizar', 'Arango', '2011-08-08', 'M', 'Carrera 7 # 156-68, Bogotá', '3160123456', 'est.felipe.villamizar@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2017-01-30', 'Activo'),
(31, 'TI', '1054329876', 'Catalina', 'Maria', 'Zamora', 'Parra', '2012-01-23', 'F', 'Carrera 18 # 82-55, Bogotá', '3182345678', 'est.catalina.zamora@littlehorizons.edu.co', 'O+', 'Ninguna', 'Ninguna', '2018-01-29', 'Activo'),
(32, 'Pasaporte', 'CO982345', 'Arthur', 'Emile', 'Dubois', 'Ramirez', '2018-10-10', 'M', 'Carrera 9 # 72-35, Bogotá', '3168901234', 'est.arthur.dubois@littlehorizons.edu.co', 'O-', 'Ninguna', 'Ninguna', '2024-01-22', 'Activo');

-- 6. Relación Estudiante - Acudiente (35 relaciones con coherencia familiar)
INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, es_contacto_emergencia) VALUES
(1, 1, TRUE, TRUE), (1, 2, FALSE, TRUE),
(2, 1, TRUE, TRUE), (2, 2, FALSE, TRUE),
(18, 1, TRUE, TRUE), (18, 2, FALSE, TRUE),
(3, 3, TRUE, TRUE), (3, 4, FALSE, TRUE),
(19, 3, TRUE, TRUE), (19, 4, FALSE, TRUE),
(4, 5, TRUE, TRUE), (4, 6, FALSE, TRUE),
(5, 5, TRUE, TRUE), (5, 6, FALSE, TRUE),
(20, 5, TRUE, TRUE), (20, 6, FALSE, TRUE),
(6, 7, TRUE, TRUE), (6, 8, FALSE, TRUE),
(32, 7, TRUE, TRUE), (32, 8, FALSE, TRUE),
(7, 9, TRUE, TRUE), (7, 10, FALSE, TRUE),
(21, 9, TRUE, TRUE), (21, 10, FALSE, TRUE),
(8, 11, TRUE, TRUE), (8, 12, FALSE, TRUE),
(22, 11, TRUE, TRUE), (22, 12, FALSE, TRUE),
(9, 13, TRUE, TRUE), (9, 14, FALSE, TRUE),
(23, 13, TRUE, TRUE), (23, 14, FALSE, TRUE),
(10, 15, TRUE, TRUE), (10, 16, FALSE, TRUE),
(24, 15, TRUE, TRUE), (24, 16, FALSE, TRUE),
(11, 17, TRUE, TRUE), (11, 18, FALSE, TRUE),
(25, 17, TRUE, TRUE), (25, 18, FALSE, TRUE),
(12, 19, TRUE, TRUE), (12, 20, FALSE, TRUE),
(26, 19, TRUE, TRUE), (26, 20, FALSE, TRUE),
(13, 21, TRUE, TRUE), (13, 22, FALSE, TRUE),
(27, 21, TRUE, TRUE), (27, 22, FALSE, TRUE),
(14, 23, TRUE, TRUE), (14, 24, FALSE, TRUE),
(28, 23, TRUE, TRUE), (28, 24, FALSE, TRUE),
(15, 25, TRUE, TRUE), (15, 26, FALSE, TRUE),
(29, 25, TRUE, TRUE), (29, 26, FALSE, TRUE),
(16, 27, TRUE, TRUE), (16, 28, FALSE, TRUE),
(30, 27, TRUE, TRUE), (30, 28, FALSE, TRUE),
(17, 29, TRUE, TRUE), (17, 30, FALSE, TRUE),
(31, 29, TRUE, TRUE), (31, 30, FALSE, TRUE);

-- 7. Matrículas (32 registros de matrículas en el año lectivo 2026)
INSERT INTO matriculas (id_matricula, id_estudiante, id_grado, año_academico, estado_matricula, fecha_matricula, observaciones) VALUES
(1, 1, 1, 2026, 'Activa', '2026-01-20', 'Matrícula en Preescolar A'),
(2, 2, 1, 2026, 'Activa', '2026-01-20', 'Matrícula en Preescolar B'),
(3, 3, 2, 2026, 'Activa', '2026-01-21', 'Matrícula en Grado Primero'),
(4, 4, 3, 2026, 'Activa', '2026-01-21', 'Matrícula en Grado Segundo'),
(5, 5, 5, 2026, 'Activa', '2026-01-22', 'Matrícula en Grado Cuarto'),
(6, 6, 4, 2026, 'Activa', '2026-01-22', 'Matrícula en Grado Tercero'),
(7, 7, 6, 2026, 'Activa', '2026-01-22', 'Matrícula en Grado Quinto'),
(8, 8, 7, 2026, 'Activa', '2026-01-23', 'Matrícula en Grado Sexto'),
(9, 9, 8, 2026, 'Activa', '2026-01-23', 'Matrícula en Grado Séptimo'),
(10, 10, 9, 2026, 'Activa', '2026-01-23', 'Matrícula en Grado Octavo'),
(11, 11, 10, 2026, 'Activa', '2026-01-24', 'Matrícula en Grado Noveno'),
(12, 12, 11, 2026, 'Activa', '2026-01-24', 'Matrícula en Grado Décimo'),
(13, 13, 12, 2026, 'Activa', '2026-01-24', 'Matrícula en Grado Once'),
(14, 14, 12, 2026, 'Activa', '2026-01-24', 'Matrícula en Grado Once'),
(15, 15, 12, 2026, 'Activa', '2026-01-24', 'Matrícula en Grado Once'),
(16, 16, 11, 2026, 'Activa', '2026-01-25', 'Matrícula en Grado Décimo'),
(17, 17, 10, 2026, 'Activa', '2026-01-25', 'Matrícula en Grado Noveno'),
(18, 18, 1, 2026, 'Activa', '2026-01-20', 'Matrícula en Preescolar A'),
(19, 19, 4, 2026, 'Activa', '2026-01-21', 'Matrícula en Grado Tercero'),
(20, 20, 7, 2026, 'Activa', '2026-01-21', 'Matrícula en Grado Sexto'),
(21, 21, 3, 2026, 'Activa', '2026-01-22', 'Matrícula en Grado Segundo'),
(22, 22, 5, 2026, 'Activa', '2026-01-22', 'Matrícula en Grado Cuarto'),
(23, 23, 6, 2026, 'Activa', '2026-01-22', 'Matrícula en Grado Quinto'),
(24, 24, 7, 2026, 'Activa', '2026-01-23', 'Matrícula en Grado Sexto'),
(25, 25, 8, 2026, 'Activa', '2026-01-23', 'Matrícula en Grado Séptimo'),
(26, 26, 9, 2026, 'Activa', '2026-01-23', 'Matrícula en Grado Octavo'),
(27, 27, 10, 2026, 'Activa', '2026-01-24', 'Matrícula en Grado Noveno'),
(28, 28, 12, 2025, 'Completada', '2025-01-20', 'Graduado Promoción 2025'),
(29, 29, 11, 2026, 'Activa', '2026-01-25', 'Matrícula en Grado Décimo'),
(30, 30, 9, 2026, 'Activa', '2026-01-23', 'Matrícula en Grado Octavo'),
(31, 31, 8, 2026, 'Activa', '2026-01-23', 'Matrícula en Grado Séptimo'),
(32, 32, 2, 2026, 'Activa', '2026-01-21', 'Matrícula en Grado Primero');


-- 8. Profesores (30 registros)
INSERT INTO profesores (id_profesor, tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, correo_electronico, telefono, especializacion, fecha_contratacion, estado) VALUES
(1, 'CC', '80888001', 'Guillermo', 'Leon', 'Valencia', 'Munoz', 'guillermo.valencia@littlehorizons.edu.co', '3001000001', 'Matemáticas y Cálculo', '2018-01-15', 'Activo'),
(2, 'CC', '52777002', 'Beatriz', 'Elena', 'Pinzon', 'Solano', 'beatriz.pinzon@littlehorizons.edu.co', '3001000002', 'Lengua Castellana y Literatura', '2018-01-15', 'Activo'),
(3, 'CC', '79666003', 'Armando', 'Esteban', 'Mendoza', 'Saenz', 'armando.mendoza@littlehorizons.edu.co', '3001000003', 'Ciencias Sociales e Historia', '2019-02-01', 'Activo'),
(4, 'CC', '51555004', 'Marcela', 'Maria', 'Valencia', 'Perez', 'marcela.valencia@littlehorizons.edu.co', '3001000004', 'Biología y Educación Ambiental', '2019-02-01', 'Activo'),
(5, 'CE', '444005', 'Nicholas', 'James', 'Smith', 'Taylor', 'nicholas.smith@littlehorizons.edu.co', '3001000005', 'Inglés Avanzado', '2020-01-10', 'Activo'),
(6, 'CC', '80333006', 'Hermes', 'Antonio', 'Pinzon', 'Galarza', 'hermes.pinzon@littlehorizons.edu.co', '3001000006', 'Física Clásica y Moderna', '2017-08-01', 'Activo'),
(7, 'CC', '52222007', 'Julia', 'Mercedes', 'Solano', 'Galindo', 'julia.solano@littlehorizons.edu.co', '3001000007', 'Química General y Orgánica', '2019-07-15', 'Activo'),
(8, 'CC', '79111008', 'Mario', 'Alberto', 'Calderon', 'Duarte', 'mario.calderon@littlehorizons.edu.co', '3001000008', 'Educación Física y Deportes', '2020-01-20', 'Activo'),
(9, 'CC', '51000009', 'Patricia', 'Ines', 'Fernandez', 'Peralta', 'patricia.fernandez@littlehorizons.edu.co', '3001000009', 'Artes Plásticas y Visuales', '2021-01-18', 'Activo'),
(10, 'CC', '80999010', 'Hugo', 'Enrique', 'Lombardi', 'Carrillo', 'hugo.lombardi@littlehorizons.edu.co', '3001000010', 'Música y Expresión Artística', '2016-01-15', 'Activo'),
(11, 'CC', '52888011', 'Aura', 'Maria', 'Fuentes', 'Rico', 'aura.fuentes@littlehorizons.edu.co', '3001000011', 'Pedagogía Infantil / Preescolar', '2022-01-17', 'Activo'),
(12, 'CC', '51777012', 'Ines', 'Ramona', 'Ramirez', 'Santos', 'ines.ramirez@littlehorizons.edu.co', '3001000012', 'Básica Primaria Integrada', '2015-02-01', 'Activo'),
(13, 'CC', '79666013', 'Freddy', 'Stewart', 'Contreras', 'Diaz', 'freddy.contreras@littlehorizons.edu.co', '3001000013', 'Informática y Tecnología', '2021-07-26', 'Activo'),
(14, 'CC', '52555014', 'Sandra', 'Paola', 'Patino', 'Duque', 'sandra.patino@littlehorizons.edu.co', '3001000014', 'Filosofía y Ética', '2018-08-01', 'Activo'),
(15, 'CC', '80444015', 'Roberto', 'Carlos', 'Mendoza', 'Nieto', 'roberto.mendoza@littlehorizons.edu.co', '3001000015', 'Economía y Ciencias Políticas', '2019-01-22', 'Activo'),
(16, 'CE', '555016', 'Sarah', 'Louise', 'Jenkins', 'Brown', 'sarah.jenkins@littlehorizons.edu.co', '3001000016', 'Francés y Literatura Universal', '2022-01-10', 'Activo'),
(17, 'CC', '51333017', 'Bertha', 'Eugenia', 'Munoz', 'Cortes', 'bertha.munoz@littlehorizons.edu.co', '3001000017', 'Geografía y Democracia', '2017-01-23', 'Activo'),
(18, 'CC', '79222018', 'Wilson', 'Javier', 'Sastoque', 'Mejia', 'wilson.sastoque@littlehorizons.edu.co', '3001000018', 'Robótica y Mecatrónica', '2023-01-16', 'Activo'),
(19, 'CC', '52111019', 'Mariana', 'Lucia', 'Valdes', 'Jimenez', 'mariana.valdes@littlehorizons.edu.co', '3001000019', 'Básica Primaria Lenguaje', '2020-08-03', 'Activo'),
(20, 'CC', '80000020', 'Daniel', 'Alfonso', 'Valencia', 'Perez', 'daniel.valencia@littlehorizons.edu.co', '3001000020', 'Educación Religiosa y Valores', '2018-01-15', 'Activo'),
(21, 'CC', '51999021', 'Sofia', 'Isabel', 'Lopez', 'Carvajal', 'sofia.lopez@littlehorizons.edu.co', '3001000021', 'Preescolar y Estimulación', '2023-01-16', 'Activo'),
(22, 'CC', '79888022', 'Gabriel', 'Jaime', 'Arango', 'Osorio', 'gabriel.arango@littlehorizons.edu.co', '3001000022', 'Álgebra y Trigonometría', '2019-01-22', 'Activo'),
(23, 'CC', '52777023', 'Catalina', 'Maria', 'Gomez', 'Echeverri', 'catalina.gomez@littlehorizons.edu.co', '3001000023', 'Ecología y Sostenibilidad', '2021-01-18', 'Activo'),
(24, 'CC', '80666024', 'Julian', 'David', 'Londono', 'Bedoya', 'julian.londono@littlehorizons.edu.co', '3001000024', 'Expresión Corporal y Danza', '2022-08-01', 'Activo'),
(25, 'CC', '51555025', 'Adriana', 'Lucia', 'Bernal', 'Sierra', 'adriana.bernal@littlehorizons.edu.co', '3001000025', 'Comprensión Lectora Primaria', '2020-01-20', 'Activo'),
(26, 'CC', '79444026', 'Mauricio', 'Alejandro', 'Cadavid', 'Gil', 'mauricio.cadavid@littlehorizons.edu.co', '3001000026', 'Química Orgánica Avanzada', '2018-01-15', 'Activo'),
(27, 'CC', '52333027', 'Paula', 'Andrea', 'Restrepo', 'Velez', 'paula.restrepo@littlehorizons.edu.co', '3001000027', 'Física Mecánica y Termodinámica', '2019-07-15', 'Activo'),
(28, 'CC', '80222028', 'Santiago', 'Manuel', 'Rios', 'Guzman', 'santiago.rios@littlehorizons.edu.co', '3001000028', 'Estadística y Probabilidad', '2021-01-18', 'Activo'),
(29, 'CC', '51111029', 'Gloria', 'Amparo', 'Jaramillo', 'Cano', 'gloria.jaramillo@littlehorizons.edu.co', '3001000029', 'Historia de Colombia y Cívica', '2016-08-01', 'Activo'),
(30, 'CC', '79000030', 'Felipe', 'Eduardo', 'Morales', 'Suarez', 'felipe.morales@littlehorizons.edu.co', '3001000030', 'Natación y Acondicionamiento', '2022-01-10', 'Activo');

-- 9. Materias (30 materias por grados)
INSERT INTO materias (id_materia, codigo_materia, nombre_materia, descripcion, id_grado, horas_semanales, esta_activa) VALUES
(1, 'PRE-DIM', 'Dimensión Cognitiva y Comunicativa', 'Desarrollo integral para preescolar', 1, 10, TRUE),
(2, 'MAT-1', 'Matemáticas Básicas 1°', 'Aritmética básica, conteo y figuras geométricas', 2, 5, TRUE),
(3, 'ESP-1', 'Español y Lectoescritura 1°', 'Iniciación a la lectura y escritura guiada', 2, 5, TRUE),
(4, 'ING-1', 'Inglés Inicial 1°', 'Vocabulario básico, canciones e interacción oral', 2, 3, TRUE),
(5, 'MAT-2', 'Matemáticas 2°', 'Sumas, restas y resolución de problemas simples', 3, 5, TRUE),
(6, 'ESP-2', 'Español 2°', 'Lectura comprensiva, caligrafía y oraciones', 3, 5, TRUE),
(7, 'CN-2', 'Ciencias Naturales 2°', 'Seres vivos, plantas y el entorno natural', 3, 3, TRUE),
(8, 'MAT-3', 'Matemáticas 3°', 'Multiplicación, división elemental y medidas', 4, 5, TRUE),
(9, 'ESP-3', 'Lengua Castellana 3°', 'Gramática básica, redacción y ortografía', 4, 4, TRUE),
(10, 'SOC-3', 'Ciencias Sociales 3°', 'Comunidad, geografía del municipio y normas', 4, 3, TRUE),
(11, 'MAT-4', 'Matemáticas 4°', 'Fracciones, geometría y problemas aplicados', 5, 5, TRUE),
(12, 'ESP-4', 'Lengua Castellana 4°', 'Comprensión de textos narrativos y argumentativos', 5, 4, TRUE),
(13, 'CN-4', 'Ciencias Naturales 4°', 'Ecosistemas, energía y materia', 5, 3, TRUE),
(14, 'MAT-5', 'Matemáticas 5°', 'Operaciones con decimales, áreas y volúmenes', 6, 5, TRUE),
(15, 'ESP-5', 'Lengua Castellana 5°', 'Géneros literarios, producción textual y análisis', 6, 4, TRUE),
(16, 'ING-5', 'Inglés B1 5°', 'Tiempos verbales, escucha y redacción básica', 6, 4, TRUE),
(17, 'MAT-6', 'Aritmética y Razonamiento 6°', 'Números enteros, racionales y proporcionalidad', 7, 5, TRUE),
(18, 'BIO-6', 'Biología Celular 6°', 'Célula, tejidos y reinos de la naturaleza', 7, 4, TRUE),
(19, 'MAT-7', 'Álgebra Inicial 7°', 'Expresiones algebraicas y ecuaciones de primer grado', 8, 5, TRUE),
(20, 'SOC-7', 'Historia Universal y Geografía 7°', 'Civilizaciones antiguas y geografía continental', 8, 3, TRUE),
(21, 'ALG-8', 'Álgebra Intermedia 8°', 'Factorización, fracciones algebraicas y funciones', 9, 5, TRUE),
(22, 'FIS-8', 'Fundamentos de Física 8°', 'Cinemática, movimiento rectilíneo y magnitudes', 9, 4, TRUE),
(23, 'ALG-9', 'Álgebra y Trigonometría Básica 9°', 'Sistemas de ecuaciones lineales y funciones cuadráticas', 10, 5, TRUE),
(24, 'QUI-9', 'Química Inorgánica 9°', 'Tabla periódica, enlaces químicos y nomenclatura', 10, 4, TRUE),
(25, 'TRI-10', 'Trigonometría y Geometría Analítica 10°', 'Identidades trigonométricas y secciones cónicas', 11, 5, TRUE),
(26, 'FIS-10', 'Física Clásica 10°', 'Leyes de Newton, trabajo, potencia y energía', 11, 4, TRUE),
(27, 'QUI-10', 'Química Orgánica 10°', 'Hidrocarburos, grupos funcionales y reacciones', 11, 4, TRUE),
(28, 'CAL-11', 'Cálculo Diferencial e Integral 11°', 'Límites, derivadas, integrales y optimización', 12, 5, TRUE),
(29, 'FIS-11', 'Física Electromagnética y Ondas 11°', 'Ondas, óptica, termodinámica y electromagnetismo', 12, 4, TRUE),
(30, 'FIL-11', 'Filosofía Contemporánea y Ética 11°', 'Epistemología, bioética y debate filosófico', 12, 3, TRUE);

-- 10. Asignaciones de Profesores (30 asignaciones)
INSERT INTO asignaciones_profesor (id_asignacion, id_profesor, id_materia, año_academico, seccion) VALUES
(1, 11, 1, 2026, 'A'), (2, 21, 1, 2026, 'B'),
(3, 12, 2, 2026, 'A'), (4, 19, 3, 2026, 'A'),
(5, 5, 4, 2026, 'A'), (6, 12, 5, 2026, 'A'),
(7, 25, 6, 2026, 'A'), (8, 4, 7, 2026, 'A'),
(9, 1, 8, 2026, 'A'), (10, 2, 9, 2026, 'A'),
(11, 3, 10, 2026, 'A'), (12, 1, 11, 2026, 'A'),
(13, 2, 12, 2026, 'A'), (14, 4, 13, 2026, 'A'),
(15, 22, 14, 2026, 'A'), (16, 2, 15, 2026, 'A'),
(17, 5, 16, 2026, 'A'), (18, 22, 17, 2026, 'A'),
(19, 4, 18, 2026, 'A'), (20, 22, 19, 2026, 'A'),
(21, 3, 20, 2026, 'A'), (22, 1, 21, 2026, 'A'),
(23, 6, 22, 2026, 'A'), (24, 22, 23, 2026, 'A'),
(25, 7, 24, 2026, 'A'), (26, 1, 25, 2026, 'A'),
(27, 27, 26, 2026, 'A'), (28, 26, 27, 2026, 'A'),
(29, 1, 28, 2026, 'A'), (30, 6, 29, 2026, 'A');

-- 11. Inscripciones en Materias (35 inscripciones)
INSERT INTO inscripciones_materia (id_inscripcion, id_estudiante, id_materia, año_academico, periodo_academico, seccion, fecha_inscripcion, estado) VALUES
(1, 1, 1, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(2, 2, 1, 2026, 'I', 'B', '2026-01-20', 'Activa'),
(3, 18, 1, 2026, 'I', 'A', '2026-01-20', 'Activa'),
(4, 3, 2, 2026, 'I', 'A', '2026-01-21', 'Activa'),
(5, 3, 3, 2026, 'I', 'A', '2026-01-21', 'Activa'),
(6, 32, 2, 2026, 'I', 'A', '2026-01-21', 'Activa'),
(7, 32, 3, 2026, 'I', 'A', '2026-01-21', 'Activa'),
(8, 4, 5, 2026, 'I', 'A', '2026-01-21', 'Activa'),
(9, 21, 5, 2026, 'I', 'A', '2026-01-22', 'Activa'),
(10, 6, 8, 2026, 'I', 'A', '2026-01-22', 'Activa'),
(11, 19, 8, 2026, 'I', 'A', '2026-01-22', 'Activa'),
(12, 5, 11, 2026, 'I', 'A', '2026-01-22', 'Activa'),
(13, 22, 11, 2026, 'I', 'A', '2026-01-22', 'Activa'),
(14, 7, 14, 2026, 'I', 'A', '2026-01-22', 'Activa'),
(15, 23, 14, 2026, 'I', 'A', '2026-01-22', 'Activa'),
(16, 8, 17, 2026, 'I', 'A', '2026-01-23', 'Activa'),
(17, 20, 17, 2026, 'I', 'A', '2026-01-23', 'Activa'),
(18, 24, 17, 2026, 'I', 'A', '2026-01-23', 'Activa'),
(19, 9, 19, 2026, 'I', 'A', '2026-01-23', 'Activa'),
(20, 25, 19, 2026, 'I', 'A', '2026-01-23', 'Activa'),
(21, 31, 19, 2026, 'I', 'A', '2026-01-23', 'Activa'),
(22, 10, 21, 2026, 'I', 'A', '2026-01-23', 'Activa'),
(23, 26, 21, 2026, 'I', 'A', '2026-01-23', 'Activa'),
(24, 30, 21, 2026, 'I', 'A', '2026-01-23', 'Activa'),
(25, 11, 23, 2026, 'I', 'A', '2026-01-24', 'Activa'),
(26, 17, 23, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(27, 27, 23, 2026, 'I', 'A', '2026-01-24', 'Activa'),
(28, 12, 25, 2026, 'I', 'A', '2026-01-24', 'Activa'),
(29, 16, 25, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(30, 29, 25, 2026, 'I', 'A', '2026-01-25', 'Activa'),
(31, 13, 28, 2026, 'I', 'A', '2026-01-24', 'Activa'),
(32, 14, 28, 2026, 'I', 'A', '2026-01-24', 'Activa'),
(33, 15, 28, 2026, 'I', 'A', '2026-01-24', 'Activa'),
(34, 13, 29, 2026, 'I', 'A', '2026-01-24', 'Activa'),
(35, 14, 30, 2026, 'I', 'A', '2026-01-24', 'Activa');

-- 12. Registros Académicos / Calificaciones (35 registros según escala MEN Dec. 1290)
INSERT INTO registros_academicos (id_registro, id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, observaciones, fecha_registro) VALUES
(1, 1, 1, 11, 2026, 'I', 4.8, 'Desempeño Superior: Excelente integración y curiosidad cognitiva.', '2026-03-30'),
(2, 2, 1, 21, 2026, 'I', 4.5, 'Desempeño Alto: Muy buen desarrollo del lenguaje y socialización.', '2026-03-30'),
(3, 18, 1, 11, 2026, 'I', 4.2, 'Desempeño Alto: Participa activamente en dinámicas de grupo.', '2026-03-30'),
(4, 3, 2, 12, 2026, 'I', 4.7, 'Desempeño Superior: Gran agilidad en cálculo mental básico.', '2026-03-30'),
(5, 3, 3, 19, 2026, 'I', 4.3, 'Desempeño Alto: Buena lectura fluida y ortografía inicial.', '2026-03-30'),
(6, 32, 2, 12, 2026, 'I', 3.8, 'Desempeño Básico: Requiere afianzar conteo hacia atrás.', '2026-03-30'),
(7, 32, 3, 19, 2026, 'I', 4.0, 'Desempeño Alto: Progresa favorablemente en escritura.', '2026-03-30'),
(8, 4, 5, 12, 2026, 'I', 4.9, 'Desempeño Superior: Impecable dominio en resolución de problemas.', '2026-03-30'),
(9, 21, 5, 12, 2026, 'I', 3.6, 'Desempeño Básico: Necesita mayor concentración en operaciones combinadas.', '2026-03-30'),
(10, 6, 8, 1, 2026, 'I', 4.6, 'Desempeño Superior: Excelente comprensión de tablas de multiplicar.', '2026-03-30'),
(11, 19, 8, 1, 2026, 'I', 3.4, 'Desempeño Básico: Debe repasar divisiones por una cifra.', '2026-03-30'),
(12, 5, 11, 1, 2026, 'I', 4.4, 'Desempeño Alto: Buen manejo de fracciones homogéneas.', '2026-03-30'),
(13, 22, 11, 1, 2026, 'I', 2.8, 'Desempeño Bajo: Dificultad severa en fracciones y geometría (Requiere plan de mejora).', '2026-03-30'),
(14, 7, 14, 22, 2026, 'I', 4.8, 'Desempeño Superior: Extraordinaria capacidad analítica y lógica.', '2026-03-30'),
(15, 23, 14, 22, 2026, 'I', 3.7, 'Desempeño Básico: Cumple con los objetivos esenciales del período.', '2026-03-30'),
(16, 8, 17, 22, 2026, 'I', 4.1, 'Desempeño Alto: Resuelve ecuaciones aditivas con enteros adecuadamente.', '2026-03-30'),
(17, 20, 17, 22, 2026, 'I', 3.9, 'Desempeño Básico: Presenta trabajos a tiempo, reforzar signos.', '2026-03-30'),
(18, 24, 17, 22, 2026, 'I', 4.5, 'Desempeño Alto: Participación activa y buen desempeño en talleres.', '2026-03-30'),
(19, 9, 19, 22, 2026, 'I', 4.3, 'Desempeño Alto: Buena interpretación de lenguaje algebraico.', '2026-03-30'),
(20, 25, 19, 22, 2026, 'I', 3.5, 'Desempeño Básico: Aprobó evaluaciones con apoyo tutorial.', '2026-03-30'),
(21, 31, 19, 22, 2026, 'I', 4.7, 'Desempeño Superior: Destacada destreza en simplificación de polinomios.', '2026-03-30'),
(22, 10, 21, 1, 2026, 'I', 4.9, 'Desempeño Superior: Domina todos los casos de factorización.', '2026-03-30'),
(23, 26, 21, 1, 2026, 'I', 3.2, 'Desempeño Básico: Requiere afianzar trinomio de la forma ax2+bx+c.', '2026-03-30'),
(24, 30, 21, 1, 2026, 'I', 4.0, 'Desempeño Alto: Cumple con el estándar esperado para grado octavo.', '2026-03-30'),
(25, 11, 23, 22, 2026, 'I', 4.6, 'Desempeño Superior: Plantea y soluciona sistemas lineales 2x2 y 3x3.', '2026-03-30'),
(26, 17, 23, 22, 2026, 'I', 4.2, 'Desempeño Alto: Buen análisis de parábolas y funciones cuadráticas.', '2026-03-30'),
(27, 27, 23, 22, 2026, 'I', 2.5, 'Desempeño Bajo: Reprobó el período por reiteradas fallas a talleres.', '2026-03-30'),
(28, 12, 25, 1, 2026, 'I', 4.8, 'Desempeño Superior: Demostraciones trigonométricas impecables.', '2026-03-30'),
(29, 16, 25, 1, 2026, 'I', 4.3, 'Desempeño Alto: Buen manejo de teoremas de seno y coseno.', '2026-03-30'),
(30, 29, 25, 1, 2026, 'I', 3.7, 'Desempeño Básico: Logra resolver triángulos oblicuángulos.', '2026-03-30'),
(31, 13, 28, 1, 2026, 'I', 5.0, 'Desempeño Superior: Máxima excelencia en cálculo de derivadas y límites.', '2026-03-30'),
(32, 14, 28, 1, 2026, 'I', 4.7, 'Desempeño Superior: Excelente aplicación de la regla de la cadena.', '2026-03-30'),
(33, 15, 28, 1, 2026, 'I', 4.4, 'Desempeño Alto: Buen rigor formal en teoremas de valor medio.', '2026-03-30'),
(34, 13, 29, 6, 2026, 'I', 4.9, 'Desempeño Superior: Destacado rendimiento en laboratorio de ondas.', '2026-03-30'),
(35, 14, 30, 14, 2026, 'I', 4.6, 'Desempeño Superior: Ensayos filosóficos de alto nivel argumentativo.', '2026-03-30');

-- 13. Asistencias Diarias (35 registros)
INSERT INTO asistencias (id_asistencia, id_estudiante, id_materia, fecha_asistencia, estado_asistencia, observaciones, registrado_por) VALUES
(1, 1, 1, '2026-02-02', 'Presente', 'Asistencia puntual', 11),
(2, 2, 1, '2026-02-02', 'Presente', 'Asistencia puntual', 21),
(3, 18, 1, '2026-02-02', 'Presente', 'Asistencia puntual', 11),
(4, 3, 2, '2026-02-02', 'Presente', 'Asistencia puntual', 12),
(5, 32, 2, '2026-02-02', 'Tardanza', 'Llegó 15 min tarde por congestión en ruta', 12),
(6, 4, 5, '2026-02-02', 'Presente', 'Asistencia puntual', 12),
(7, 21, 5, '2026-02-02', 'Ausente', 'Falta injustificada', 12),
(8, 6, 8, '2026-02-02', 'Presente', 'Asistencia puntual', 1),
(9, 19, 8, '2026-02-02', 'Excusa Médica', 'Incapacidad médica por virosis', 1),
(10, 5, 11, '2026-02-02', 'Presente', 'Asistencia puntual', 1),
(11, 22, 11, '2026-02-02', 'Ausente', 'Falta reiterada sin soporte', 1),
(12, 7, 14, '2026-02-02', 'Presente', 'Asistencia puntual', 22),
(13, 23, 14, '2026-02-02', 'Presente', 'Asistencia puntual', 22),
(14, 8, 17, '2026-02-02', 'Presente', 'Asistencia puntual', 22),
(15, 20, 17, '2026-02-02', 'Presente', 'Asistencia puntual', 22),
(16, 24, 17, '2026-02-02', 'Presente', 'Asistencia puntual', 22),
(17, 9, 19, '2026-02-02', 'Presente', 'Asistencia puntual', 22),
(18, 25, 19, '2026-02-02', 'Tardanza', 'Ingreso tardío con autorización', 22),
(19, 31, 19, '2026-02-02', 'Presente', 'Asistencia puntual', 22),
(20, 10, 21, '2026-02-02', 'Presente', 'Asistencia puntual', 1),
(21, 26, 21, '2026-02-02', 'Ausente', 'Ausencia sin justificación', 1),
(22, 30, 21, '2026-02-02', 'Presente', 'Asistencia puntual', 1),
(23, 11, 23, '2026-02-02', 'Presente', 'Asistencia puntual', 22),
(24, 17, 23, '2026-02-02', 'Presente', 'Asistencia puntual', 22),
(25, 27, 23, '2026-02-02', 'Ausente', 'Alerta: 3 ausencias acumuladas en la semana', 22),
(26, 12, 25, '2026-02-02', 'Presente', 'Asistencia puntual', 1),
(27, 16, 25, '2026-02-02', 'Presente', 'Asistencia puntual', 1),
(28, 29, 25, '2026-02-02', 'Presente', 'Asistencia puntual', 1),
(29, 13, 28, '2026-02-02', 'Presente', 'Asistencia puntual', 1),
(30, 14, 28, '2026-02-02', 'Presente', 'Asistencia puntual', 1),
(31, 15, 28, '2026-02-02', 'Presente', 'Asistencia puntual', 1),
(32, 13, 29, '2026-02-03', 'Presente', 'Asistencia puntual', 6),
(33, 14, 30, '2026-02-03', 'Presente', 'Asistencia puntual', 14),
(34, 27, 23, '2026-02-03', 'Ausente', 'Segunda falta consecutiva en la semana', 22),
(35, 27, 23, '2026-02-04', 'Ausente', 'Tercera falta consecutiva: se notifica a bienestar', 22);


-- 14. Clubes Extracurriculares (30 clubes)
INSERT INTO clubes (id_club, nombre_club, descripcion, id_profesor_supervisor, capacidad_maxima, horario_reunion, esta_activo) VALUES
(1, 'Club de Robótica y Automatización', 'Diseño, ensamble y programación de robots con Arduino y Lego Mindstorms', 18, 25, 'Miércoles 3:30 - 5:30 PM', TRUE),
(2, 'Club de Astronomía y Astrofísica', 'Observación telescópica, constelaciones y cosmología básica', 6, 30, 'Viernes 6:00 - 8:00 PM', TRUE),
(3, 'Club de Debate y Modelo ONU', 'Oratoria, argumentación, diplomacia y simulación de Naciones Unidas', 15, 30, 'Lunes 3:30 - 5:00 PM', TRUE),
(4, 'Club de Artes Plásticas y Cerámica', 'Técnicas de pintura en óleo, acuarela, escultura y modelado en arcilla', 9, 20, 'Martes 3:30 - 5:30 PM', TRUE),
(5, 'Club de Fútbol Juvenil', 'Entrenamiento táctico, físico y participación en torneos intercolegiales', 8, 30, 'Lunes y Jueves 4:00 - 6:00 PM', TRUE),
(6, 'Club de Baloncesto Femenino y Masculino', 'Desarrollo de fundamentos, jugadas de pizarra y acondicionamiento', 8, 25, 'Martes y Viernes 4:00 - 5:30 PM', TRUE),
(7, 'Club de Ajedrez y Estrategia', 'Aperturas, medio juego, finales y desarrollo del pensamiento analítico', 1, 20, 'Jueves 3:30 - 5:00 PM', TRUE),
(8, 'Club de Coro y Ensamble Vocal', 'Técnica vocal, armonía coral y presentaciones navideñas y patrias', 10, 35, 'Miércoles 3:30 - 5:30 PM', TRUE),
(9, 'Club de Teatro y Expresión Corporal', 'Dramaturgia, improvisación, puesta en escena y montaje de obras clásicas', 24, 25, 'Viernes 3:30 - 6:00 PM', TRUE),
(10, 'Club de Periodismo Escolar y Podcast', 'Redacción de crónicas, diseño del periódico escolar y grabación de podcasts', 2, 20, 'Martes 3:30 - 5:00 PM', TRUE),
(11, 'Club de Danza Folclórica y Contemporánea', 'Ritmos tradicionales colombianos (cumbia, bambuco) y técnica contemporánea', 24, 25, 'Miércoles y Viernes 3:30 - 5:00 PM', TRUE),
(12, 'Club de Programación y Videojuegos', 'Lógica de programación en Python, Scratch y desarrollo de videojuegos 2D', 13, 25, 'Lunes 3:30 - 5:30 PM', TRUE),
(13, 'Club de Ecología y Huerto Escolar', 'Cultivos orgánicos, compostaje y proyectos de reciclaje institucional', 23, 20, 'Jueves 3:30 - 5:00 PM', TRUE),
(14, 'Club de Emprendimiento y Finanzas Personales', 'Educación financiera, formulación de planes de negocio e innovación social', 15, 25, 'Martes 3:30 - 5:00 PM', TRUE),
(15, 'Club de Literatura y Cine Foro', 'Lectura de clásicos juveniles, análisis cinematográfico y tertulias', 16, 20, 'Viernes 3:30 - 5:30 PM', TRUE),
(16, 'Club de Fotografía Digital y Edición', 'Manejo de cámaras réflex, iluminación, composición y edición digital', 9, 18, 'Jueves 3:30 - 5:30 PM', TRUE),
(17, 'Club de Voleibol Mixto', 'Técnicas de voleo, remate, bloqueo y trabajo en equipo', 30, 24, 'Lunes y Miércoles 4:00 - 5:30 PM', TRUE),
(18, 'Club de Natación y Salvamento Acuático', 'Estilos de nado (libre, pecho, espalda, mariposa) y técnicas de flotación', 30, 20, 'Martes y Jueves 3:30 - 5:00 PM', TRUE),
(19, 'Club de Gimnasia Rítmica', 'Flexibilidad, manejo de cinta, aro y rutinas musicales', 24, 18, 'Lunes y Miércoles 3:30 - 5:00 PM', TRUE),
(20, 'Club de Banda Marcial y Sinfónica', 'Instrumentos de viento, percusión y desfiles de honor', 10, 40, 'Sábados 8:00 - 11:00 AM', TRUE),
(21, 'Club de Francés y Cultura Gala', 'Conversación cotidiana, gastronomía y preparación para exámenes DELF', 16, 20, 'Jueves 3:30 - 5:00 PM', TRUE),
(22, 'Club de Matemáticas Olímpicas', 'Entrenamiento para olimpiadas nacionales e internacionales de matemáticas', 28, 20, 'Martes 3:30 - 5:30 PM', TRUE),
(23, 'Club de Ciencias y Experimentos Asombrosos', 'Química divertida, física interactiva y ferias científicas para primaria', 7, 22, 'Miércoles 3:30 - 5:00 PM', TRUE),
(24, 'Club de Primeros Auxilios y Brigada Escolar', 'Atención prehospitalaria básica, prevención de desastres y evacuación', 4, 25, 'Viernes 3:30 - 5:00 PM', TRUE),
(25, 'Club de Diseño 3D e Impresión', 'Modelado tridimensional en Tinkercad y prototipado rápido en impresoras 3D', 18, 15, 'Jueves 3:30 - 5:30 PM', TRUE),
(26, 'Club de Taekwondo y Defensa Personal', 'Filosofía marcial, formas (poomsae) y técnicas de autodefensa', 8, 25, 'Martes y Jueves 4:00 - 5:30 PM', TRUE),
(27, 'Club de Cine y Producción Audiovisual', 'Guion, dirección, actuación frente a cámara y edición de cortometrajes', 13, 20, 'Lunes 3:30 - 6:00 PM', TRUE),
(28, 'Club de Yoga y Mindfulness', 'Posturas de respiración, concentración, manejo de estrés y bienestar', 20, 20, 'Viernes 3:30 - 4:30 PM', TRUE),
(29, 'Club de Cocina y Repostería Infantil', 'Higiene alimentaria, cocina saludable y postres creativos', 23, 15, 'Miércoles 3:30 - 5:30 PM', TRUE),
(30, 'Club de Filatelia e Historia Postal', 'Coleccionismo, geopolítica y conservación de sellos mundiales', 29, 15, 'Jueves 3:30 - 4:30 PM', TRUE);

-- 15. Membresías en Clubes (32 registros)
INSERT INTO membresias_club (id_membresia, id_estudiante, id_club, fecha_ingreso, estado_membresia) VALUES
(1, 1, 4, '2026-02-05', 'Activa'), (2, 2, 8, '2026-02-05', 'Activa'),
(3, 3, 5, '2026-02-05', 'Activa'), (4, 4, 7, '2026-02-05', 'Activa'),
(5, 5, 1, '2026-02-05', 'Activa'), (6, 6, 21, '2026-02-05', 'Activa'),
(7, 7, 10, '2026-02-05', 'Activa'), (8, 8, 12, '2026-02-05', 'Activa'),
(9, 9, 5, '2026-02-05', 'Activa'), (10, 10, 9, '2026-02-05', 'Activa'),
(11, 11, 3, '2026-02-05', 'Activa'), (12, 12, 14, '2026-02-05', 'Activa'),
(13, 13, 1, '2026-02-05', 'Activa'), (14, 14, 22, '2026-02-05', 'Activa'),
(15, 15, 3, '2026-02-05', 'Activa'), (16, 16, 6, '2026-02-05', 'Activa'),
(17, 17, 16, '2026-02-05', 'Activa'), (18, 18, 29, '2026-02-05', 'Activa'),
(19, 19, 4, '2026-02-05', 'Activa'), (20, 20, 5, '2026-02-05', 'Activa'),
(21, 21, 11, '2026-02-05', 'Activa'), (22, 22, 13, '2026-02-05', 'Activa'),
(23, 23, 8, '2026-02-05', 'Activa'), (24, 24, 17, '2026-02-05', 'Activa'),
(25, 25, 2, '2026-02-05', 'Activa'), (26, 26, 1, '2026-02-05', 'Activa'),
(27, 27, 18, '2026-02-05', 'Activa'), (28, 29, 27, '2026-02-05', 'Activa'),
(29, 30, 26, '2026-02-05', 'Activa'), (30, 31, 7, '2026-02-05', 'Activa'),
(31, 32, 23, '2026-02-05', 'Activa'), (32, 13, 25, '2026-02-05', 'Activa');

-- 16. Eventos Institucionales (10 eventos)
INSERT INTO eventos (id_evento, nombre_evento, descripcion, tipo_evento, fecha_evento, lugar, tiene_competencia, id_organizador, esta_activo) VALUES
(1, 'Feria de la Ciencia y la Tecnología 2026', 'Exposición de prototipos científicos, robótica y proyectos ecológicos', 'Científico', '2026-04-15', 'Coliseo Principal y Patio Central', TRUE, 7, TRUE),
(2, 'Olimpiadas Deportivas Little Horizons', 'Competencias atléticas, torneo de fútbol, baloncesto y voleibol', 'Deportivo', '2026-05-20', 'Canchas Polideportivas', TRUE, 8, TRUE),
(3, 'Festival de las Artes y el Teatro', 'Muestras de danza folclórica, música coral y representaciones teatrales', 'Cultural', '2026-06-10', 'Auditorio Mayor San Francisco', TRUE, 10, TRUE),
(4, 'Simulación Modelo de Naciones Unidas (LH-MUN)', 'Debate intercolegial sobre geopolítica, cambio climático y paz mundial', 'Académico', '2026-08-25', 'Salones de Conferencias', TRUE, 15, TRUE),
(5, 'Spelling Bee & English Day', 'Concurso de deletreo en inglés y feria gastronómica multicultural', 'Académico', '2026-09-18', 'Auditorio Mayor', TRUE, 5, TRUE),
(6, 'Día de la Familia y la Convivencia', 'Integración de padres, acudientes y estudiantes con actividades lúdicas', 'Social', '2026-10-03', 'Zonas Verdes del Campus', FALSE, 20, TRUE),
(7, 'Olimpiadas Internas de Matemáticas', 'Prueba escrita de lógica y resolución de problemas por categorías', 'Académico', '2026-07-08', 'Aulas de Clase', TRUE, 1, TRUE),
(8, 'Torneo Relámpago de Ajedrez', 'Competencia por sistema suizo a 5 rondas con reloj de control', 'Deportivo', '2026-09-05', 'Biblioteca Central', TRUE, 1, TRUE),
(9, 'Bazar Navideño y Recital de Villancicos', 'Muestra empresarial de estudiantes y concierto del coro escolar', 'Cultural', '2026-11-28', 'Plaza Principal', FALSE, 10, TRUE),
(10, 'Expedición Ecológica Humedal La Conejera', 'Salida de campo e investigación botánica para grados de bachillerato', 'Científico', '2026-03-24', 'Humedal La Conejera', FALSE, 4, TRUE);

-- 17. Participantes en Eventos (30 participantes)
INSERT INTO participantes_evento (id_participacion, id_evento, id_estudiante, rol_participacion, posicion_obtenida, observaciones) VALUES
(1, 1, 13, 'Competidor', '1er Lugar', 'Proyecto: Brazo robótico hidráulico controlado con sensores'),
(2, 1, 5, 'Competidor', '2do Lugar', 'Proyecto: Generador de energía eólica a pequeña escala'),
(3, 1, 26, 'Competidor', '3er Lugar', 'Proyecto: Sistema automatizado de riego para huerta'),
(4, 1, 8, 'Competidor', 'Participante', 'Proyecto: Purificador casero de agua'),
(5, 1, 14, 'Organizador', NULL, 'Comité de logística y bienvenida técnica'),
(6, 2, 3, 'Competidor', '1er Lugar', 'Carrera de velocidad 50 metros infantiles'),
(7, 2, 9, 'Competidor', '1er Lugar', 'Capitán equipo de fútbol campeón'),
(8, 2, 16, 'Competidor', '2do Lugar', 'Torneo de baloncesto categoría juvenil'),
(9, 2, 20, 'Competidor', 'Participante', 'Equipo de fútbol subcampeón'),
(10, 2, 27, 'Competidor', '1er Lugar', 'Natación 100 metros estilo libre'),
(11, 3, 2, 'Participante', NULL, 'Ensamble de coro preescolar'),
(12, 3, 10, 'Participante', NULL, 'Papel principal en obra de teatro Macbeth adaptada'),
(13, 3, 21, 'Participante', NULL, 'Danza tradicional El Sanjuanero Huilense'),
(14, 3, 23, 'Participante', NULL, 'Solista en el recital de violín'),
(15, 3, 19, 'Participante', NULL, 'Muestra de pintura impresionista'),
(16, 4, 11, 'Competidor', '1er Lugar', 'Mejor Delegado en Consejo de Seguridad'),
(17, 4, 12, 'Competidor', '2do Lugar', 'Delegada destacada en Comisión de Derechos Humanos'),
(18, 4, 15, 'Competidor', '1er Lugar', 'Presidente de Asamblea General LH-MUN'),
(19, 4, 29, 'Competidor', 'Participante', 'Delegado República Francesa'),
(20, 5, 6, 'Competidor', '1er Lugar', 'Campeón Spelling Bee categoría intermedia'),
(21, 5, 7, 'Competidor', '2do Lugar', 'Finalista Spelling Bee'),
(22, 5, 25, 'Competidor', '3er Lugar', 'Semifinalista Spelling Bee'),
(23, 7, 13, 'Competidor', '1er Lugar', 'Puntaje perfecto en olimpiada de cálculo'),
(24, 7, 14, 'Competidor', '2do Lugar', 'Segundo puesto olimpiada categoría senior'),
(25, 7, 17, 'Competidor', '1er Lugar', 'Primer puesto categoría junior'),
(26, 8, 4, 'Competidor', '1er Lugar', 'Campeona invicta torneo ajedrez primaria'),
(27, 8, 31, 'Competidor', '2do Lugar', 'Subcampeón ajedrez secundaria'),
(28, 10, 22, 'Participante', NULL, 'Recolección y clasificación de muestras botánicas'),
(29, 10, 24, 'Participante', NULL, 'Registro fotográfico del ecosistema'),
(30, 10, 30, 'Participante', NULL, 'Elaboración del informe de sostenibilidad');

-- 18. Registro de Pagos (32 pagos realistas)
INSERT INTO pagos (id_pago, id_estudiante, id_acudiente, id_concepto, monto_pagado, fecha_pago, metodo_pago, numero_recibo, año_academico, periodo_academico, notas) VALUES
(1, 1, 1, 1, 550000.00, '2026-01-20', 'Transferencia', 'REC-2026-0001', 2026, 'Anual', 'Matrícula Preescolar 2026 cancelada'),
(2, 2, 1, 1, 550000.00, '2026-01-20', 'Transferencia', 'REC-2026-0002', 2026, 'Anual', 'Matrícula hermana - Descuento institucional aplicado en pensión'),
(3, 3, 3, 1, 550000.00, '2026-01-21', 'Tarjeta Débito', 'REC-2026-0003', 2026, 'Anual', 'Matrícula Grado Primero'),
(4, 4, 5, 1, 550000.00, '2026-01-21', 'Tarjeta Crédito', 'REC-2026-0004', 2026, 'Anual', 'Matrícula Grado Segundo'),
(5, 5, 5, 1, 550000.00, '2026-01-22', 'Transferencia', 'REC-2026-0005', 2026, 'Anual', 'Matrícula Grado Cuarto'),
(6, 6, 7, 1, 550000.00, '2026-01-22', 'Transferencia', 'REC-2026-0006', 2026, 'Anual', 'Matrícula Grado Tercero - Estudiante extranjero'),
(7, 7, 9, 1, 550000.00, '2026-01-22', 'Efectivo', 'REC-2026-0007', 2026, 'Anual', 'Matrícula Grado Quinto'),
(8, 8, 11, 1, 550000.00, '2026-01-23', 'Transferencia', 'REC-2026-0008', 2026, 'Anual', 'Matrícula Grado Sexto'),
(9, 9, 13, 1, 550000.00, '2026-01-23', 'Efectivo', 'REC-2026-0009', 2026, 'Anual', 'Matrícula Grado Séptimo'),
(10, 10, 15, 1, 550000.00, '2026-01-23', 'Tarjeta Débito', 'REC-2026-0010', 2026, 'Anual', 'Matrícula Grado Octavo'),
(11, 11, 17, 1, 550000.00, '2026-01-24', 'Transferencia', 'REC-2026-0011', 2026, 'Anual', 'Matrícula Grado Noveno'),
(12, 12, 19, 1, 550000.00, '2026-01-24', 'Tarjeta Crédito', 'REC-2026-0012', 2026, 'Anual', 'Matrícula Grado Décimo'),
(13, 13, 21, 1, 550000.00, '2026-01-24', 'Transferencia', 'REC-2026-0013', 2026, 'Anual', 'Matrícula Grado Once'),
(14, 14, 23, 1, 550000.00, '2026-01-24', 'Cheque', 'REC-2026-0014', 2026, 'Anual', 'Matrícula Grado Once'),
(15, 15, 25, 1, 550000.00, '2026-01-24', 'Transferencia', 'REC-2026-0015', 2026, 'Anual', 'Matrícula Grado Once'),
(16, 1, 1, 2, 380000.00, '2026-02-05', 'Transferencia', 'REC-2026-0016', 2026, 'I', 'Pensión Febrero 2026'),
(17, 2, 1, 2, 342000.00, '2026-02-05', 'Transferencia', 'REC-2026-0017', 2026, 'I', 'Pensión Febrero 2026 (10% desc. hermano)'),
(18, 3, 3, 2, 380000.00, '2026-02-05', 'Tarjeta Débito', 'REC-2026-0018', 2026, 'I', 'Pensión Febrero 2026'),
(19, 4, 5, 2, 380000.00, '2026-02-05', 'Tarjeta Crédito', 'REC-2026-0019', 2026, 'I', 'Pensión Febrero 2026'),
(20, 5, 5, 2, 342000.00, '2026-02-05', 'Transferencia', 'REC-2026-0020', 2026, 'I', 'Pensión Febrero 2026 (10% desc. hermano)'),
(21, 6, 7, 2, 380000.00, '2026-02-05', 'Transferencia', 'REC-2026-0021', 2026, 'I', 'Pensión Febrero 2026'),
(22, 7, 9, 2, 380000.00, '2026-02-06', 'Efectivo', 'REC-2026-0022', 2026, 'I', 'Pensión Febrero 2026'),
(23, 8, 11, 2, 380000.00, '2026-02-06', 'Transferencia', 'REC-2026-0023', 2026, 'I', 'Pensión Febrero 2026'),
(24, 9, 13, 2, 380000.00, '2026-02-06', 'Efectivo', 'REC-2026-0024', 2026, 'I', 'Pensión Febrero 2026'),
(25, 10, 15, 2, 380000.00, '2026-02-06', 'Tarjeta Débito', 'REC-2026-0025', 2026, 'I', 'Pensión Febrero 2026'),
(26, 11, 17, 2, 380000.00, '2026-02-07', 'Transferencia', 'REC-2026-0026', 2026, 'I', 'Pensión Febrero 2026'),
(27, 12, 19, 2, 380000.00, '2026-02-07', 'Tarjeta Crédito', 'REC-2026-0027', 2026, 'I', 'Pensión Febrero 2026'),
(28, 13, 21, 2, 380000.00, '2026-02-07', 'Transferencia', 'REC-2026-0028', 2026, 'I', 'Pensión Febrero 2026'),
(29, 14, 23, 2, 380000.00, '2026-02-08', 'Cheque', 'REC-2026-0029', 2026, 'I', 'Pensión Febrero 2026'),
(30, 15, 25, 2, 380000.00, '2026-02-08', 'Transferencia', 'REC-2026-0030', 2026, 'I', 'Pensión Febrero 2026'),
(31, 1, 1, 3, 220000.00, '2026-01-22', 'Transferencia', 'REC-2026-0031', 2026, 'Anual', 'Uniforme de diario institucional'),
(32, 13, 21, 10, 90000.00, '2026-02-05', 'Transferencia', 'REC-2026-0032', 2026, 'Anual', 'Inscripción kit de robótica y sensores');

-- 19. Facturas Electrónicas (32 facturas con CUFE SHA-384 DIAN simulado)
INSERT INTO facturas (id_factura, numero_factura, id_pago, fecha_emision, cufe, nombre_archivo_pdf, ruta_archivo) VALUES
(1, 'FE-000101', 1, '2026-01-20', '8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b', 'factura_FE-000101.pdf', 'invoices/factura_FE-000101.pdf'),
(2, 'FE-000102', 2, '2026-01-20', '9a1e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b', 'factura_FE-000102.pdf', 'invoices/factura_FE-000102.pdf'),
(3, 'FE-000103', 3, '2026-01-21', '7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e', 'factura_FE-000103.pdf', 'invoices/factura_FE-000103.pdf'),
(4, 'FE-000104', 4, '2026-01-21', '6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d', 'factura_FE-000104.pdf', 'invoices/factura_FE-000104.pdf'),
(5, 'FE-000105', 5, '2026-01-22', '5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c', 'factura_FE-000105.pdf', 'invoices/factura_FE-000105.pdf'),
(6, 'FE-000106', 6, '2026-01-22', '4a3f2e1d0c9b8a7f6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b', 'factura_FE-000106.pdf', 'invoices/factura_FE-000106.pdf'),
(7, 'FE-000107', 7, '2026-01-22', '3f2e1d0c9b8a7f6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a', 'factura_FE-000107.pdf', 'invoices/factura_FE-000107.pdf'),
(8, 'FE-000108', 8, '2026-01-23', '2e1d0c9b8a7f6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f', 'factura_FE-000108.pdf', 'invoices/factura_FE-000108.pdf'),
(9, 'FE-000109', 9, '2026-01-23', '1d0c9b8a7f6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e', 'factura_FE-000109.pdf', 'invoices/factura_FE-000109.pdf'),
(10, 'FE-000110', 10, '2026-01-23', '0c9b8a7f6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d', 'factura_FE-000110.pdf', 'invoices/factura_FE-000110.pdf'),
(11, 'FE-000111', 11, '2026-01-24', 'cb8a7f6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c', 'factura_FE-000111.pdf', 'invoices/factura_FE-000111.pdf'),
(12, 'FE-000112', 12, '2026-01-24', 'ba7f6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b', 'factura_FE-000112.pdf', 'invoices/factura_FE-000112.pdf'),
(13, 'FE-000113', 13, '2026-01-24', 'a7f6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8', 'factura_FE-000113.pdf', 'invoices/factura_FE-000113.pdf'),
(14, 'FE-000114', 14, '2026-01-24', '7f6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a', 'factura_FE-000114.pdf', 'invoices/factura_FE-000114.pdf'),
(15, 'FE-000115', 15, '2026-01-24', 'f6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7', 'factura_FE-000115.pdf', 'invoices/factura_FE-000115.pdf'),
(16, 'FE-000116', 16, '2026-02-05', '6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7f', 'factura_FE-000116.pdf', 'invoices/factura_FE-000116.pdf'),
(17, 'FE-000117', 17, '2026-02-05', 'e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7f6', 'factura_FE-000117.pdf', 'invoices/factura_FE-000117.pdf'),
(18, 'FE-000118', 18, '2026-02-05', '5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7f6e', 'factura_FE-000118.pdf', 'invoices/factura_FE-000118.pdf'),
(19, 'FE-000119', 19, '2026-02-05', 'd4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7f6e5', 'factura_FE-000119.pdf', 'invoices/factura_FE-000119.pdf'),
(20, 'FE-000120', 20, '2026-02-05', '4c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7f6e5d', 'factura_FE-000120.pdf', 'invoices/factura_FE-000120.pdf'),
(21, 'FE-000121', 21, '2026-02-05', 'c3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7f6e5d4', 'factura_FE-000121.pdf', 'invoices/factura_FE-000121.pdf'),
(22, 'FE-000122', 22, '2026-02-06', '3b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7f6e5d4c', 'factura_FE-000122.pdf', 'invoices/factura_FE-000122.pdf'),
(23, 'FE-000123', 23, '2026-02-06', 'b2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3', 'factura_FE-000123.pdf', 'invoices/factura_FE-000123.pdf'),
(24, 'FE-000124', 24, '2026-02-06', '2a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b', 'factura_FE-000124.pdf', 'invoices/factura_FE-000124.pdf'),
(25, 'FE-000125', 25, '2026-02-06', 'a1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2', 'factura_FE-000125.pdf', 'invoices/factura_FE-000125.pdf'),
(26, 'FE-000126', 26, '2026-02-07', '1f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a', 'factura_FE-000126.pdf', 'invoices/factura_FE-000126.pdf'),
(27, 'FE-000127', 27, '2026-02-07', 'f0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1', 'factura_FE-000127.pdf', 'invoices/factura_FE-000127.pdf'),
(28, 'FE-000128', 28, '2026-02-07', '0e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f', 'factura_FE-000128.pdf', 'invoices/factura_FE-000128.pdf'),
(29, 'FE-000129', 29, '2026-02-08', 'e9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f0', 'factura_FE-000129.pdf', 'invoices/factura_FE-000129.pdf'),
(30, 'FE-000130', 30, '2026-02-08', '9d8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f0e', 'factura_FE-000130.pdf', 'invoices/factura_FE-000130.pdf'),
(31, 'FE-000131', 31, '2026-01-22', 'd8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f0e9', 'factura_FE-000131.pdf', 'invoices/factura_FE-000131.pdf'),
(32, 'FE-000132', 32, '2026-02-05', '8c7b6a5f4e3d2c1b8f4c2b9a1e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f0e9d', 'factura_FE-000132.pdf', 'invoices/factura_FE-000132.pdf');

-- 20. Recursos de Biblioteca, Laboratorios y Deportes (35 recursos)
INSERT INTO recursos (id_recurso, codigo_recurso, nombre_recurso, id_categoria, descripcion, fecha_adquisicion, ubicacion_fisica, estado_recurso) VALUES
(1, 'LIB-001', 'Cien Años de Soledad - Gabriel García Márquez', 1, 'Edición conmemorativa RAE tapa dura', '2020-02-10', 'Biblioteca - Estante L1', 'Disponible'),
(2, 'LIB-002', 'El Principito - Antoine de Saint-Exupéry', 1, 'Ilustrado a color para primaria', '2021-03-15', 'Biblioteca - Estante L2', 'Disponible'),
(3, 'LIB-003', 'Álgebra de Baldor con CD', 1, 'Texto guía clásico con ejercicios resueltos', '2019-01-20', 'Biblioteca - Estante M1', 'Disponible'),
(4, 'LIB-004', 'Física Universitaria Vol. 1 - Sears & Zemansky', 1, 'Texto universitario para grado 11', '2021-08-14', 'Biblioteca - Estante C2', 'Disponible'),
(5, 'LIB-005', 'Biología de Campbell 11a Edición', 1, 'Manual de referencia en ciencias biológicas', '2022-01-10', 'Biblioteca - Estante C1', 'Disponible'),
(6, 'LIB-006', 'Don Quijote de la Mancha - Miguel de Cervantes', 1, 'Edición comentada dos tomos', '2019-05-18', 'Biblioteca - Estante L1', 'Disponible'),
(7, 'LIB-007', 'Química General - Raymond Chang', 1, 'Texto guía de química inorgánica y general', '2020-09-01', 'Biblioteca - Estante C3', 'Disponible'),
(8, 'LIB-008', 'Breve Historia de Colombia - Jorge Orlando Melo', 1, 'Historia política y social colombiana', '2021-11-05', 'Biblioteca - Estante H1', 'Disponible'),
(9, 'LIB-009', 'Cálculo de una Variable - James Stewart', 1, 'Texto guía de cálculo para grado once', '2022-02-20', 'Biblioteca - Estante M2', 'Disponible'),
(10, 'LIB-010', 'Diccionario Oxford Avanzado Inglés-Español', 1, 'Edición bilingüe encuadernada', '2020-03-12', 'Biblioteca - Referencia', 'Disponible'),
(11, 'LAB-001', 'Microscopio Óptico Binocular Olympus CX23', 2, 'Aumento 1000x con iluminación LED', '2021-06-15', 'Laboratorio de Biología', 'Disponible'),
(12, 'LAB-002', 'Microscopio Óptico Monocular Nikon E100', 2, 'Aumento 400x para nivel escolar', '2020-04-10', 'Laboratorio de Biología', 'Disponible'),
(13, 'LAB-003', 'Balanza Analítica Digital Ohaus 0.001g', 2, 'Precisión milimétrica para química', '2022-03-25', 'Laboratorio de Química', 'Disponible'),
(14, 'LAB-004', 'Set de 10 Tubos de Ensayo Pyrex con Gradilla', 2, 'Vidrio borosilicato resistente a llama', '2023-01-18', 'Laboratorio de Química', 'Disponible'),
(15, 'LAB-005', 'Mechero Bunsen con Manguera de Seguridad', 2, 'Quemador a gas con válvula de aire', '2021-02-14', 'Laboratorio de Química', 'Disponible'),
(16, 'LAB-006', 'Sensor de Fuerza y Movimiento Vernier GoDirect', 2, 'Sensor digital USB/Bluetooth para física', '2023-08-10', 'Laboratorio de Física', 'Disponible'),
(17, 'LAB-007', 'Generador de Van de Graaff Didáctico', 2, 'Generador electrostático para demostraciones', '2019-10-05', 'Laboratorio de Física', 'Disponible'),
(18, 'LAB-008', 'Kit de Disección de Acero Inoxidable 12 Piezas', 2, 'Bisturís, pinzas, tijeras y agujas', '2022-05-12', 'Laboratorio de Biología', 'Disponible'),
(19, 'LAB-009', 'pH-metro Digital Portátil Hanna Instruments', 2, 'Medidor de acidez con calibración automática', '2023-02-20', 'Laboratorio de Química', 'Disponible'),
(20, 'LAB-010', 'Espectrofotómetro Visible UV-Vis Educativo', 2, 'Análisis de absorbancia en soluciones', '2022-09-15', 'Laboratorio de Química', 'Disponible'),
(21, 'DEP-001', 'Balón de Fútbol Profesional Golty N° 5', 3, 'Balón oficial de cuero termosellado', '2023-01-10', 'Bodega de Deportes', 'Disponible'),
(22, 'DEP-002', 'Balón de Baloncesto Molten BG3800 N° 7', 3, 'Cuero sintético para interiores y exteriores', '2023-01-10', 'Bodega de Deportes', 'Disponible'),
(23, 'DEP-003', 'Balón de Voleibol Mikasa V200W', 3, 'Diseño aerodinámico oficial FIVB', '2023-01-10', 'Bodega de Deportes', 'Disponible'),
(24, 'DEP-004', 'Colchoneta de Gimnasia de Alta Densidad', 3, 'Espuma compacta forrada en lona impermeable', '2022-04-18', 'Gimnasio Cubierto', 'Disponible'),
(25, 'DEP-005', 'Set de 20 Conos de Entrenamiento y Marcación', 3, 'Conos de plástico flexible colores vivos', '2023-03-01', 'Bodega de Deportes', 'Disponible'),
(26, 'DEP-006', 'Cronómetro Digital Profesional Casio', 3, 'Memoria de 100 laps y resistencia al agua', '2021-07-20', 'Oficina de Educación Física', 'Disponible'),
(27, 'TEC-001', 'Tablet Educativa Samsung Galaxy Tab A8', 4, 'Pantalla 10.5", 64GB con funda de uso rudo', '2022-08-15', 'Carro Móvil de Tecnología 1', 'Disponible'),
(28, 'TEC-002', 'Tablet Educativa Samsung Galaxy Tab A8', 4, 'Pantalla 10.5", 64GB con funda de uso rudo', '2022-08-15', 'Carro Móvil de Tecnología 1', 'Disponible'),
(29, 'TEC-003', 'Tablet Educativa Samsung Galaxy Tab A8', 4, 'Pantalla 10.5", 64GB con funda de uso rudo', '2022-08-15', 'Carro Móvil de Tecnología 1', 'Disponible'),
(30, 'TEC-004', 'Portátil Lenovo ThinkPad L14 Gen 3', 4, 'Intel Core i5, 16GB RAM, SSD 512GB', '2023-01-20', 'Sala de Profesores', 'Disponible'),
(31, 'TEC-005', 'Calculadora Científica Casio FX-991LAX', 4, 'Calculadora avanzada con display natural', '2021-02-18', 'Biblioteca - Reserva', 'Disponible'),
(32, 'TEC-006', 'Kit de Robótica Lego Education Spike Prime', 4, 'Kit completo con motores, sensores y hub', '2023-02-10', 'Laboratorio de Robótica', 'Disponible'),
(33, 'TEC-007', 'Impresora 3D Creality Ender 3 S1 Pro', 4, 'Volumen 220x220x270mm boquilla alta temperatura', '2023-05-14', 'Laboratorio de Robótica', 'Disponible'),
(34, 'DID-001', 'Esqueleto Humano Articulado Tamaño Natural', 5, 'Modelo anatómico de 170cm sobre soporte móvil', '2019-08-25', 'Laboratorio de Biología', 'Disponible'),
(35, 'DID-002', 'Globo Terráqueo Político y Físico con Luz LED', 5, 'Diámetro 30cm con relieve orográfico', '2020-01-15', 'Sala de Ciencias Sociales', 'Disponible');

-- 21. Préstamos a Estudiantes (30 registros)
INSERT INTO prestamos_estudiante (id_prestamo, id_recurso, id_estudiante, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, estado_prestamo, observaciones) VALUES
(1, 1, 14, '2026-02-02', '2026-02-09', '2026-02-08', 'Devuelto', 'Entregado en perfecto estado'),
(2, 2, 3, '2026-02-03', '2026-02-10', '2026-02-09', 'Devuelto', 'Lectura de plan lector grado 1'),
(3, 3, 10, '2026-02-04', '2026-02-11', '2026-02-11', 'Devuelto', 'Taller de factorización'),
(4, 4, 13, '2026-02-05', '2026-02-12', '2026-02-12', 'Devuelto', 'Estudio para olimpiadas de física'),
(5, 5, 11, '2026-02-06', '2026-02-13', '2026-02-13', 'Devuelto', 'Consulta genética mendeliana'),
(6, 6, 15, '2026-02-09', '2026-02-16', '2026-02-15', 'Devuelto', 'Ensayo literario grado 11'),
(7, 7, 12, '2026-02-10', '2026-02-17', '2026-02-17', 'Devuelto', 'Estudio de nomenclatura orgánica'),
(8, 8, 8, '2026-02-11', '2026-02-18', '2026-02-18', 'Devuelto', 'Trabajo de ciencias sociales'),
(9, 9, 14, '2026-02-12', '2026-02-19', '2026-02-18', 'Devuelto', 'Cálculo de derivadas implícitas'),
(10, 10, 6, '2026-02-13', '2026-02-20', '2026-02-20', 'Devuelto', 'Preparación Spelling Bee'),
(11, 11, 13, '2026-02-16', '2026-02-16', '2026-02-16', 'Devuelto', 'Práctica de tinción de bacterias'),
(12, 12, 7, '2026-02-16', '2026-02-16', '2026-02-16', 'Devuelto', 'Observación de células vegetales de cebolla'),
(13, 13, 12, '2026-02-17', '2026-02-17', '2026-02-17', 'Devuelto', 'Determinación de densidad de metales'),
(14, 16, 14, '2026-02-18', '2026-02-18', '2026-02-18', 'Devuelto', 'Medición de aceleración en plano inclinado'),
(15, 21, 9, '2026-02-19', '2026-02-19', '2026-02-19', 'Devuelto', 'Práctica de penaltis torneo escolar'),
(16, 22, 16, '2026-02-19', '2026-02-19', '2026-02-19', 'Devuelto', 'Entrenamiento de tiros libres'),
(17, 23, 24, '2026-02-20', '2026-02-20', '2026-02-20', 'Devuelto', 'Juego de integración voleibol'),
(18, 26, 8, '2026-02-23', '2026-02-23', '2026-02-23', 'Devuelto', 'Toma de tiempos prueba 100m'),
(19, 27, 4, '2026-02-24', '2026-02-24', '2026-02-24', 'Devuelto', 'Uso en taller de lectura digital'),
(20, 28, 5, '2026-02-24', '2026-02-24', '2026-02-24', 'Devuelto', 'Módulo de matemáticas interactivas'),
(21, 31, 11, '2026-02-25', '2026-03-04', '2026-03-03', 'Devuelto', 'Examen bimestral de trigonometría'),
(22, 32, 13, '2026-02-26', '2026-02-26', '2026-02-26', 'Devuelto', 'Programación de seguidor de línea'),
(23, 1, 15, '2026-03-02', '2026-03-09', NULL, 'Activo', 'Préstamo vigente biblioteca'),
(24, 3, 26, '2026-03-02', '2026-03-09', NULL, 'Activo', 'Préstamo vigente biblioteca'),
(25, 4, 14, '2026-03-03', '2026-03-10', NULL, 'Activo', 'Estudio de termodinámica'),
(26, 9, 13, '2026-03-03', '2026-03-10', NULL, 'Activo', 'Integrales por sustitución'),
(27, 27, 7, '2026-03-04', '2026-03-04', NULL, 'Activo', 'Sesión de aula virtual'),
(28, 31, 16, '2026-03-04', '2026-03-11', NULL, 'Activo', 'Préstamo para taller de física'),
(29, 3, 22, '2026-02-01', '2026-02-08', NULL, 'Vencido', 'Pendiente de devolución: Notificado acudiente'),
(30, 8, 20, '2026-02-03', '2026-02-10', NULL, 'Vencido', 'Pendiente de devolución en biblioteca');

-- 22. Préstamos a Profesores (15 registros)
INSERT INTO prestamos_profesor (id_prestamo, id_recurso, id_profesor, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, estado_prestamo, observaciones) VALUES
(1, 11, 4, '2026-02-02', '2026-02-06', '2026-02-06', 'Devuelto', 'Laboratorio de biología grado 7'),
(2, 13, 7, '2026-02-03', '2026-02-05', '2026-02-05', 'Devuelto', 'Práctica de estequiometría grado 10'),
(3, 16, 6, '2026-02-04', '2026-02-08', '2026-02-08', 'Devuelto', 'Demostración de cinemática grado 9'),
(4, 17, 6, '2026-02-05', '2026-02-05', '2026-02-05', 'Devuelto', 'Clase de electrostática grado 11'),
(5, 20, 26, '2026-02-09', '2026-02-13', '2026-02-13', 'Devuelto', 'Análisis espectral en química orgánica'),
(6, 21, 8, '2026-02-10', '2026-02-28', '2026-02-28', 'Devuelto', 'Mes de entrenamientos torneo fútbol'),
(7, 26, 30, '2026-02-11', '2026-02-11', '2026-02-11', 'Devuelto', 'Evaluación física trimestral'),
(8, 30, 2, '2026-02-12', '2026-02-19', '2026-02-19', 'Devuelto', 'Carga de notas del primer corte'),
(9, 32, 18, '2026-02-16', '2026-02-20', '2026-02-20', 'Devuelto', 'Taller extracurricular de robótica'),
(10, 33, 18, '2026-02-17', '2026-02-24', '2026-02-24', 'Devuelto', 'Impresión de piezas para prototipo'),
(11, 34, 4, '2026-02-23', '2026-02-27', '2026-02-27', 'Devuelto', 'Clase de anatomía y osteología humana'),
(12, 35, 3, '2026-02-24', '2026-03-03', '2026-03-03', 'Devuelto', 'Geopolítica y división continental'),
(13, 19, 7, '2026-03-02', '2026-03-09', NULL, 'Activo', 'Préstamo vigente para titulación ácida'),
(14, 30, 15, '2026-03-02', '2026-03-09', NULL, 'Activo', 'Preparación de material Modelo ONU'),
(15, 33, 13, '2026-02-05', '2026-02-12', NULL, 'Vencido', 'En préstamo extendido sin renovación formal');

-- ============================================================================
-- VERIFICACIÓN DE TOTALES Y REGLAS DE NEGOCIO
-- ============================================================================
SELECT 'estudiantes' AS Tabla, COUNT(*) AS Total FROM estudiantes
UNION ALL SELECT 'acudientes', COUNT(*) FROM acudientes
UNION ALL SELECT 'profesores', COUNT(*) FROM profesores
UNION ALL SELECT 'materias', COUNT(*) FROM materias
UNION ALL SELECT 'registros_academicos', COUNT(*) FROM registros_academicos
UNION ALL SELECT 'pagos', COUNT(*) FROM pagos
UNION ALL SELECT 'clubes', COUNT(*) FROM clubes
UNION ALL SELECT 'recursos', COUNT(*) FROM recursos;
