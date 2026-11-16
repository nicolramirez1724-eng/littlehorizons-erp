package com.littlehorizons.erp.service.export;

import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Exports invoices in structured TXT format optimized for automatic parsing
 * and AI grading agents as requested by the instructor.
 */
public class TxtInvoiceExporter implements InvoiceExporter {

    @Override
    public String export(Invoice invoice, Payment payment) throws Exception {
        File dir = new File("invoices");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = String.format("factura_%s.txt", invoice.getInvoiceNumber());
        File file = new File(dir, fileName);

        double subtotal = payment.getPaidAmount();
        double iva = 0.00; // Servicios educativos excluidos de IVA según Art. 476 Numeral 6 E.T.
        double total = subtotal + iva;

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            pw.println("================================================================================");
            pw.println("COLEGIO INTERNACIONAL LITTLE HORIZONS S.A.S. - FACTURA ELECTRONICA DE VENTA");
            pw.println("================================================================================");
            pw.println("NUMERO_FACTURA: " + invoice.getInvoiceNumber());
            pw.println("FECHA_EMISION: " + invoice.getIssueDate());
            pw.println("HORA_EMISION: 12:00:00");
            pw.println("CUFE: " + invoice.getCufe());
            pw.println("RESOLUCION_DIAN: N° 18764012345678 (Vigencia 2026-01-15 a 2027-12-31 | FE-000001 a FE-999999)");
            pw.println("REGIMEN_FISCAL: Responsable de IVA - Persona Juridica");
            pw.println();
            pw.println("[EMISOR]");
            pw.println("RAZON_SOCIAL: Colegio Internacional Little Horizons S.A.S.");
            pw.println("NIT: 901.458.732-4");
            pw.println("DIRECCION: Calle 127 # 15-45, Bogota D.C., Colombia");
            pw.println("TELEFONO: +57 (601) 745-8900");
            pw.println("CORREO_ELECTRONICO: facturacion@littlehorizons.edu.co");
            pw.println();
            pw.println("[ADQUIRENTE / RESPONSABLE FINANCIERO]");
            pw.println("NOMBRE_COMPLETO: " + (payment.getGuardianName() != null ? payment.getGuardianName() : "No registrado"));
            pw.println("NUMERO_DOCUMENTO: " + (payment.getGuardianDocNumber() != null ? payment.getGuardianDocNumber() : "222222222222"));
            pw.println("DIRECCION: " + (payment.getGuardianAddress() != null ? payment.getGuardianAddress() : "Bogota D.C."));
            pw.println("TELEFONO: " + (payment.getGuardianPhone() != null ? payment.getGuardianPhone() : "3000000000"));
            pw.println("ESTUDIANTE_BENEFICIARIO: " + (payment.getStudentName() != null ? payment.getStudentName() : "Estudiante"));
            pw.println("DOCUMENTO_ESTUDIANTE: " + (payment.getStudentDocNumber() != null ? payment.getStudentDocNumber() : "N/A"));
            pw.println();
            pw.println("[DETALLE_ITEMS]");
            pw.println("ITEM | CODIGO | DESCRIPCION | CANTIDAD | VALOR_UNITARIO_COP | IVA_PCT | SUBTOTAL_COP");
            pw.printf("1 | %s | %s | 1 | %.2f | 0.00%% (Art 476 Num 6 ET Excluido) | %.2f%n",
                    payment.getConceptCode() != null ? payment.getConceptCode() : "PAGO",
                    payment.getConceptName() != null ? payment.getConceptName() : "Servicio Educativo",
                    subtotal,
                    subtotal);
            pw.println();
            pw.println("[RESUMEN_TRIBUTARIO_COP]");
            pw.printf("SUBTOTAL: %.2f%n", subtotal);
            pw.println("DESCUENTOS: 0.00");
            pw.printf("IVA_0_PCT_EXCLUIDO: %.2f%n", iva);
            pw.printf("TOTAL_A_PAGAR: %.2f%n", total);
            pw.println("METODO_PAGO: " + (payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "Transferencia"));
            pw.println("NUMERO_RECIBO_SOPORTE: " + (payment.getReceiptNumber() != null ? payment.getReceiptNumber() : "N/A"));
            pw.println("ANIO_ACADEMICO: " + (payment.getAcademicYear() != null ? payment.getAcademicYear() : 2026));
            pw.println("PERIODO_ACADEMICO: " + (payment.getAcademicPeriod() != null ? payment.getAcademicPeriod() : "I"));
            pw.println();
            pw.println("[CUMPLIMIENTO_DIAN]");
            pw.println("TIPO_DOCUMENTO: Factura Electronica de Venta (UBL 2.1)");
            pw.println("QR_VALIDACION_DIAN: https://catalogo-vpfe.dian.gov.co/document/searchqr?documentkey=" + invoice.getCufe());
            pw.println("NOTAS_LEGALES: Documento soporte emitido bajo las disposiciones del Decreto 358 de 2020 y Resolucion 000042 de 2020.");
            pw.println("================================================================================");
        }

        return file.getAbsolutePath();
    }

    @Override
    public String getFormatName() {
        return "TXT (Legible por Agentes)";
    }

    @Override
    public String getFileExtension() {
        return ".txt";
    }
}
