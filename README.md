# Little Horizons ERP - Sistema de Gestión Educativa y Administrativa

Sistema de Planificación de Recursos Empresariales (ERP) desarrollado en Java para el **Colegio Internacional "Little Horizons"** (Preescolar a Grado 11°). Diseñado bajo las directrices del **Ministerio de Educación Nacional de Colombia (MEN)**, el **Estatuto Tributario Nacional** y las normativas de facturación electrónica de la **DIAN**.

---

## 1. Título del Proyecto
**Sistema ERP Little Horizons - Enterprise Resource Planning para Instituciones Educativas bajo Marco Legal y Tributario Colombiano**

---

## 2. Descripción

### Resumen del Propósito
El Colegio Internacional "Little Horizons" atiende a más de 500 estudiantes con 40 docentes desde nivel Preescolar hasta Grado 11°. Este ERP centraliza y automatiza integralmente sus operaciones académicas, de bienestar extracurricular, gestión de activos y administración financiera.

### Problemas que Resuelve
* **Pérdida de información académica:** Digitalización segura de calificaciones con respaldo relacional y cálculo automático de promedios.
* **Desorganización extracurricular:** Control centralizado de membresías en clubes con cupos máximos y registro de ganadores en competencias.
* **Caos en facturación y cumplimiento tributario:** Cumplimiento de la normativa DIAN mediante generación de facturas con CUFE SHA-384, exención de IVA en educación formal (Art. 476 Num. 6 E.T.) y exportación multi-formato interactiva.
* **Control de recursos y activos:** Seguimiento estricto del ciclo de préstamo y devolución de libros, equipos de laboratorio, dispositivos tecnológicos y material deportivo con detección de préstamos vencidos.
* **Seguimiento del ausentismo:** Detección automática de estudiantes con inasistencias crónicas (>3 ausencias por semana) para intervención oportuna.

### Funcionalidades Principales
1. Gestión de estudiantes y acudientes con documentos oficiales avalados por el MEN / SIMAT (RC, TI, CC, CE, Pasaporte, PPT).
2. Registro y evaluación académica conforme a la escala nacional cualitativa del **Decreto 1290 de 2009 del MEN**.
3. Control diario de asistencias con alertas de ausentismo semanal.
4. Módulo extracurricular de clubes con validación de capacidad máxima y eventos competitivos.
5. Facturación electrónica configurable vía consola en **PDF** (OpenPDF), **TXT** (estructurado clave-valor para agentes de calificación), **XML** (estándar UBL 2.1 / MUISCA DIAN) o **todos los formatos simultáneamente**.
6. Gestión de inventario de recursos y préstamos a estudiantes y profesores.
7. Auditoría estricta de todos los campos de texto y prevención de caídas de ejecución.

---

## 3. Autores
* **Desarrollador / Autor Principal:** Karen Nicol Perez
* **Correo Electrónico:** karen.nicol.perez@littlehorizons.edu.co
* **Rol:** Desarrolladora de Software Senior - Arquitectura y Backend Java

---

## 4. Tecnologías Utilizadas
* **Lenguaje de Programación:** Java SE 17+ (Compilado y verificado en OpenJDK 27).
* **Entorno de Desarrollo (IDE):** IntelliJ IDEA / Estándar Java IDE.
* **Gestor de Construcción:** Maven (`pom.xml`) y ejecución directa mediante CLI Java.
* **Base de Datos:** MySQL 8.0+ (Motor InnoDB, soporte de CHECK constraints y Foreign Keys).
* **Librerías Externas:**
  * `com.mysql:mysql-connector-j:8.3.0` (Driver JDBC oficial de MySQL).
  * `com.github.librepdf:openpdf:1.3.39` (Generación de facturas electrónicas visuales en PDF).

---

## 5. Requerimientos Previos
* **Java Development Kit (JDK):** Versión 17, 21 o superior (instalado con variables de entorno configuradas).
* **MySQL Server:** **Versión 8.0 o superior (REQUERIDO)** para la correcta evaluación de restricciones `CHECK` e índices funcionales.
* **Espacio en Disco:** Mínimo 200 MB para logs y archivos de factura generados (`invoices/`).

---

## 6. Configuración de la Base de Datos

### 6.1. Creación de la Base de Datos
El script oficial se encuentra ubicado en `docs/database.sql`. Para inicializar la base de datos completa con estructura DDL y datos DML, ejecute en su cliente MySQL (o terminal):

```sql
SOURCE docs/database.sql;
```

### 6.2. Configuración de Credenciales
La aplicación incluye el archivo `database.properties` en la raíz del proyecto para modificar la conexión sin necesidad de recompilar:

```properties
db.url=jdbc:mysql://localhost:3306/erp_little_horizons?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota&characterEncoding=UTF-8
db.user=root
db.password=
```

### 6.3. Instalación del Driver JDBC y Justificación Técnica de Tipos SIMAT
* **Driver JDBC:** Se incluye en la carpeta `lib/` (`mysql-connector-j-8.3.0.jar`) y en `pom.xml`.
* **Justificación de Documentos SIMAT:** En el esquema original solo se preveían 4 tipos. Siguiendo las directrices del MEN y la Registraduría Nacional del Estado Civil, se incorporaron:
  * `RC` (Registro Civil): Obligatorio para estudiantes de preescolar y primeros grados (< 7 años).
  * `PPT` (Permiso por Protección Temporal, Decreto 216/2021): Garantiza el derecho fundamental a la educación para población migrante.

---

## 7. Instalación y Ejecución

### 7.1. Clonar o Abrir el Repositorio
```bash
git clone <URL_DEL_REPOSITORIO>
cd LittleHorizons
```

### 7.2. Abrir en el IDE
* En **IntelliJ IDEA**, seleccione `File > Open` y elija la carpeta `LittleHorizons`. El IDE detectará automáticamente la configuración de módulos y dependencias de la carpeta `lib/` y `pom.xml`.

### 7.3. Compilación desde Terminal
Si prefiere compilar mediante línea de comandos con el JDK instalado:

```powershell
javac -encoding UTF-8 -cp "lib/*;src" -d "out/production/LittleHorizons" (Get-ChildItem -Path "src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName)
```

### 7.4. Ejecución del Sistema
```powershell
java -cp "out/production/LittleHorizons;lib/*" com.littlehorizons.erp.Main
```

---

## 8. Estructura del Proyecto y Arquitectura MVC

El código sigue estrictamente el patrón Modelo-Vista-Controlador (MVC), con código en inglés y consola en español:

```
src/com/littlehorizons/erp/
├── Main.java                          // Punto de entrada de la aplicación
├── model/                             // Entidades del dominio (POJOs)
│   ├── Student.java, Guardian.java, Teacher.java, Subject.java
│   ├── AcademicRecord.java, Attendance.java, Club.java, Event.java
│   ├── Payment.java, PaymentConcept.java, Invoice.java, Resource.java, etc.
│   ├── CertificateData.java, CertificateSubjectLine.java, CertificateGrade.java
├── dao/                               // Acceso a datos con PreparedStatement y try-with-resources
│   ├── StudentDAO.java, GuardianDAO.java, TeacherDAO.java, SubjectDAO.java
│   ├── AcademicRecordDAO.java, AttendanceDAO.java, ClubDAO.java, EventDAO.java
│   ├── PaymentDAO.java, InvoiceDAO.java, ResourceDAO.java, CertificateDAO.java
├── service/                           // Lógica de negocio y marco legal colombiano
│   ├── AcademicService.java           // Escala cualitativa MEN (Dec. 1290) y cálculos con Streams
│   ├── AttendanceService.java         // Detección de ausentismo semanal
│   ├── CertificateService.java        // Certificado de notas 2025: consulta, promedios y escritura del .txt
│   ├── CertificateFormatter.java      // Formato de texto plano alineado del certificado
│   ├── ExtracurricularService.java    // Validación de cupo de clubes y competencias
│   ├── FinanceService.java            // Pagos, cartera deudora y orquestación de facturación
│   ├── ResourceService.java           // Préstamos, inventario y vencimientos
│   └── export/                        // Patrón Strategy para facturación multi-formato
│       ├── InvoiceExporter.java       // Interfaz Strategy
│       ├── PdfInvoiceExporter.java    // Factura gráfica en PDF con OpenPDF
│       ├── TxtInvoiceExporter.java    // Factura clave-valor para Agentes Calificadores
│       ├── XmlInvoiceExporter.java    // Factura UBL 2.1 / MUISCA DIAN
│       └── InvoiceExportManager.java  // Orquestador multi-formato (PDF, TXT, XML, ALL)
├── controller/                        // Coordinación entre Vista y Negocio
│   ├── StudentController.java, AcademicController.java, TeacherController.java
│   ├── ExtracurricularController.java, FinanceController.java, ResourceController.java
│   ├── CertificateController.java
├── view/                              // Interfaz de usuario por consola con validaciones
│   ├── MainMenuView.java, StudentView.java, AcademicView.java, TeacherView.java
│   ├── ExtracurricularView.java, FinanceView.java, ResourceView.java, ReportView.java
│   ├── CertificateView.java
└── util/                              // Utilidades transversales y auditoría de entradas
    ├── DatabaseConnection.java        // Singleton de conexión JDBC
    ├── LoggerManager.java             // Registro a error_log.txt
    ├── InputValidator.java            // Expresiones regulares y auditoría de campos de texto
    ├── CufeGenerator.java             // Hash SHA-384 DIAN para CUFE
    └── TerminalReader.java            // Lector seguro de consola a prueba de excepciones
```

---

## 9. Principios y Patrones de Diseño Aplicados

### 9.1. Principios SOLID
1. **Single Responsibility Principle (SRP):** Cada clase tiene una única responsabilidad. Por ejemplo, `CufeGenerator` únicamente calcula el hash SHA-384 del CUFE, mientras que `PdfInvoiceExporter` se encarga de la maquetación visual del PDF.
2. **Open/Closed Principle (OCP):** La exportación de facturas se rige por la interfaz `InvoiceExporter`. Si en el futuro se requiere exportar a JSON o EDI, solo se añade una nueva clase implementadora sin modificar el código existente.
3. **Liskov Substitution Principle (LSP):** Cualquier implementación de `InvoiceExporter` (`PdfInvoiceExporter`, `TxtInvoiceExporter`, `XmlInvoiceExporter`) puede intercambiarse transparentemente dentro del `InvoiceExportManager`.
4. **Interface Segregation Principle (ISP):** Las interfaces y métodos en los DAOs son específicos para sus entidades, evitando clases sobrecargadas con operaciones innecesarias.
5. **Dependency Inversion Principle (DIP):** Los controladores y vistas dependen de abstracciones y servicios de negocio, facilitando pruebas unitarias desacopladas.

### 9.2. Patrones de Diseño
* **Singleton:** Aplicado en `DatabaseConnection` para centralizar la configuración de la conexión JDBC y en `LoggerManager`.
* **Data Access Object (DAO):** Desacopla la persistencia SQL de la lógica de dominio.
* **Strategy Pattern:** Implementado en la exportación de facturas (`InvoiceExporter`), permitiendo seleccionar en tiempo de ejecución si se genera PDF, TXT, XML o todos.

---

## 10. Funcionalidades Principales por Módulo

### 1. Gestión de Estudiantes
* Registro con validación estricta de documentos según el estándar SIMAT del MEN (`RC`, `TI`, `CC`, `CE`, `Pasaporte`, `PPT`).
* Asociación de acudientes con parentesco, responsabilidades financieras y contactos de emergencia.
* Matrícula por año lectivo en los grados correspondientes (Preescolar a Once).

### 2. Gestión Académica (Decreto 1290 de 2009)
* Registro de notas en escala colombiana de `0.0` a `5.0`.
* Homologación automática a la escala nacional de desempeño:
  * **Superior:** `4.6` a `5.0`
  * **Alto:** `4.0` a `4.5`
  * **Básico:** `3.0` a `3.9` (Aprobado)
  * **Bajo:** `0.0` a `2.9` (Reprobado)
* Cálculo automático de promedios por período y curso con Streams de Java.
* Control de asistencia diaria con alertas de inasistencia semanal (>3 ausencias).

### 3. Gestión de Profesores
* Directorio docente con especialidad, contactos y contrato.
* Asignación de carga académica por materia, grado y sección.

### 4. Actividades Extracurriculares
* Catálogo de 30 clubes con validación de cupo máximo.
* Calendario de eventos y registro de participantes con asignación de podio (1er, 2do, 3er lugar).

### 5. Gestión Financiera y Facturación Electrónica DIAN
* Catálogo de tarifas escolares con exención de IVA en educación formal (**Artículo 476 Numeral 6 del Estatuto Tributario**).
* Registro detallado de pagos con número de recibo consecutivo.
* Emisión interactiva de facturas electrónicas con CUFE SHA-384:
  * **Opción 1:** Generar archivo **PDF** visual profesional.
  * **Opción 2:** Generar archivo **TXT** clave-valor estructurado para evaluación por agentes automáticos.
  * **Opción 3:** Generar archivo **XML** bajo estándar UBL 2.1 / MUISCA DIAN.
  * **Opción 4:** Generar **todos los formatos simultáneamente**.
* Detección y listado de acudientes y estudiantes morosos.

### 6. Gestión de Recursos (Biblioteca y Laboratorios)
* Catálogo de libros, equipos de laboratorio, tecnología y deportes.
* Ciclo de préstamo y devolución con actualización inmediata del estado (`Disponible` / `Prestado`).
* Reporte de préstamos vencidos.

### 7. TEST - Generar Certificado de Notas (opción 8 del menú principal)
* Solicita el ID del estudiante, valida que sea numérico y verifica que exista y tenga notas del año 2025.
* Relaciona `estudiantes`, `matriculas`, `grados`, `registros_academicos` y `materias`, filtrando por `año_academico = 2025`.
* Calcula el promedio por materia (solo períodos con nota) y el promedio general del año, con un decimal.
* Genera `certificado_[id]_2025.txt` en la carpeta raíz del proyecto, con columnas alineadas y `---` en los períodos sin nota.

---

## 11. Manejo de Errores y Logs
* **Archivo de Logs:** Todos los fallos SQL y excepciones son registrados en `error_log.txt` con fecha, hora, tipo de excepción y traza de la pila.
* **Control Defensivo en Consola:** `TerminalReader` previene caídas por tipos de datos erróneos (`InputMismatchException`, `NumberFormatException`) y sanitiza cadenas (`trim()`).
* **Manejo Seguro de Recursos:** Todas las conexiones, sentencias y conjuntos de resultados se administran con `try-with-resources`.

---

## 12. Evidencia de Ejecución
* Las facturas generadas se almacenan automáticamente en la carpeta `invoices/`:
  * `invoices/factura_FE-000101.pdf` (Documento visual con tablas, datos fiscales, CUFE y QR).
  * `invoices/factura_FE-000101.txt` (Documento estructurado en texto plano legible por agentes IA).
  * `invoices/factura_FE-000101.xml` (Documento XML estándar UBL 2.1 / MUISCA DIAN).

---

## 13. Problemas Conocidos y Limitaciones
* La aplicación asume un entorno de consola estándar UTF-8. En consolas Windows heredadas con páginas de códigos distintas a CP65001, los caracteres especiales ASCII de los bordes pueden requerir el comando `chcp 65001`.

---

## 14. Futuras Mejoras
* Módulo de mensajería SMS/Email para notificación inmediata de inasistencias y boletines a los acudientes.
* Interfaz gráfica web moderna mediante Spring Boot y React.
* Integración directa con el Web Service de validación previa en vivo de la DIAN.

---

## 15. Licencia
Este proyecto ha sido desarrollado con fines académicos para el Colegio Internacional Little Horizons bajo los términos de la Licencia MIT.

---

## 16. Contacto
Para soporte técnico o consultas sobre la implementación:
* **Autor:** Karen Nicol Perez
* **Correo:** karen.nicol.perez@littlehorizons.edu.co
* **Institución:** Colegio Internacional Little Horizons S.A.S.
