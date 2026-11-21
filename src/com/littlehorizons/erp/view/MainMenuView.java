package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.*;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.TerminalReader;

public class MainMenuView {

    private final StudentView studentView;
    private final AcademicView academicView;
    private final TeacherView teacherView;
    private final ExtracurricularView extracurricularView;
    private final FinanceView financeView;
    private final ResourceView resourceView;
    private final ReportView reportView;
    private final CertificateView certificateView;

    public MainMenuView() {
        this.studentView = new StudentView(new StudentController());
        this.academicView = new AcademicView(new AcademicController());
        this.teacherView = new TeacherView(new TeacherController());
        this.extracurricularView = new ExtracurricularView(new ExtracurricularController());
        this.financeView = new FinanceView(new FinanceController());
        this.resourceView = new ResourceView(new ResourceController());
        this.reportView = new ReportView();
        this.certificateView = new CertificateView(new CertificateController());
    }

    public void start() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║            SISTEMA ERP COLEGIO LITTLE HORIZONS               ║");
        System.out.println("║        Sistema Integrado de Gestión Educativa y DIAN         ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        // Verify connection at startup
        boolean connected = DatabaseConnection.getInstance().testConnection();
        if (connected) {
            System.out.println("  [✓] Conexión a Base de Datos MySQL establecida exitosamente.");
        } else {
            System.out.println("  [!] AVISO: No se pudo conectar a MySQL en 'localhost:3306/erp_little_horizons'.");
            System.out.println("      Verifique que el servicio MySQL esté iniciado y el script database.sql ejecutado.");
            System.out.println("      Consulte el archivo database.properties para modificar credenciales.");
        }

        boolean exit = false;
        while (!exit) {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║                       MENÚ PRINCIPAL                         ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            System.out.println("  1. Gestión de Estudiantes");
            System.out.println("  2. Gestión Académica (Notas, Boletines, Asistencias MEN)");
            System.out.println("  3. Gestión de Profesores");
            System.out.println("  4. Actividades Extracurriculares (Clubes y Eventos)");
            System.out.println("  5. Gestión Financiera (Pagos y Facturación Electrónica DIAN)");
            System.out.println("  6. Gestión de Recursos (Biblioteca y Laboratorios)");
            System.out.println("  7. Reportes, Estadísticas y Auditoría");
            System.out.println("  8. TEST - Generar Certificado de Notas");
            System.out.println("  9. Salir del Sistema");
            System.out.println("────────────────────────────────────────────────────────────────");

            int option = TerminalReader.readInt("Seleccione una opción", 1, 9);
            switch (option) {
                case 1:
                    studentView.displayMenu();
                    break;
                case 2:
                    academicView.displayMenu();
                    break;
                case 3:
                    teacherView.displayMenu();
                    break;
                case 4:
                    extracurricularView.displayMenu();
                    break;
                case 5:
                    financeView.displayMenu();
                    break;
                case 6:
                    resourceView.displayMenu();
                    break;
                case 7:
                    reportView.displayMenu();
                    break;
                case 8:
                    certificateView.generateCertificate();
                    break;
                case 9:
                    exit = true;
                    System.out.println("\nGracias por utilizar el ERP Colegio Little Horizons. ¡Hasta pronto!");
                    break;
            }
        }
    }
}
