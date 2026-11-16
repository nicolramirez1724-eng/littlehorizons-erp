package com.littlehorizons.erp.service.export;

import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Exports invoices in professional visual PDF format compliant with DIAN standards
 * using the OpenPDF library.
 */
public class PdfInvoiceExporter implements InvoiceExporter {

    @Override
    public String export(Invoice invoice, Payment payment) throws Exception {
        File dir = new File("invoices");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = String.format("factura_%s.pdf", invoice.getInvoiceNumber());
        File file = new File(dir, fileName);

        double subtotal = payment.getPaidAmount();
        double iva = 0.00; // Excluido Art. 476 Num 6 ET
        double total = subtotal + iva;

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Font schoolTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, new Color(20, 50, 95));
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
        Font headerCellFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.GRAY);

        // Header Table (School Info + Invoice Box)
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{60f, 40f});

        // Left: School info
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.addElement(new Paragraph("COLEGIO INTERNACIONAL LITTLE HORIZONS S.A.S.", schoolTitleFont));
        leftCell.addElement(new Paragraph("NIT: 901.458.732-4 | Régimen Común", normalFont));
        leftCell.addElement(new Paragraph("Calle 127 # 15-45, Bogotá D.C., Colombia", normalFont));
        leftCell.addElement(new Paragraph("Tel: +57 (601) 745-8900 | facturacion@littlehorizons.edu.co", normalFont));
        leftCell.addElement(new Paragraph("Aprobación Oficial MEN Resolución N° 4589 de 2012", smallFont));
        headerTable.addCell(leftCell);

        // Right: Invoice number & DIAN resolution box
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBackgroundColor(new Color(245, 247, 250));
        rightCell.setBorderColor(new Color(20, 50, 95));
        rightCell.setBorderWidth(1.5f);
        rightCell.setPadding(8);
        rightCell.addElement(new Paragraph("FACTURA ELECTRÓNICA DE VENTA", subtitleFont));
        rightCell.addElement(new Paragraph("N° " + invoice.getInvoiceNumber(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(180, 40, 40))));
        rightCell.addElement(new Paragraph("Fecha de Emisión: " + invoice.getIssueDate(), normalFont));
        rightCell.addElement(new Paragraph("Resolución DIAN N° 18764012345678", smallFont));
        rightCell.addElement(new Paragraph("Rango: FE-000001 a FE-999999 (Vigencia 24 meses)", smallFont));
        headerTable.addCell(rightCell);

        document.add(headerTable);
        document.add(new Paragraph(" "));

        // Customer Info Table
        PdfPTable clientTable = new PdfPTable(2);
        clientTable.setWidthPercentage(100);
        clientTable.setWidths(new float[]{50f, 50f});

        PdfPCell clientHeader = new PdfPCell(new Paragraph("DATOS DEL CLIENTE Y RESPONSABLE FINANCIERO", headerCellFont));
        clientHeader.setColspan(2);
        clientHeader.setBackgroundColor(new Color(20, 50, 95));
        clientHeader.setPadding(4);
        clientTable.addCell(clientHeader);

        PdfPCell c1 = new PdfPCell();
        c1.setPadding(5);
        c1.addElement(new Paragraph("Acudiente: " + (payment.getGuardianName() != null ? payment.getGuardianName() : "No registrado"), normalFont));
        c1.addElement(new Paragraph("Doc. Identidad: " + (payment.getGuardianDocNumber() != null ? payment.getGuardianDocNumber() : "N/A"), normalFont));
        c1.addElement(new Paragraph("Dirección: " + (payment.getGuardianAddress() != null ? payment.getGuardianAddress() : "Bogotá D.C."), normalFont));
        clientTable.addCell(c1);

        PdfPCell c2 = new PdfPCell();
        c2.setPadding(5);
        c2.addElement(new Paragraph("Estudiante: " + (payment.getStudentName() != null ? payment.getStudentName() : "Estudiante"), normalFont));
        c2.addElement(new Paragraph("Doc. Estudiante: " + (payment.getStudentDocNumber() != null ? payment.getStudentDocNumber() : "N/A"), normalFont));
        c2.addElement(new Paragraph("Teléfono Acudiente: " + (payment.getGuardianPhone() != null ? payment.getGuardianPhone() : "N/A"), normalFont));
        clientTable.addCell(c2);

        document.add(clientTable);
        document.add(new Paragraph(" "));

        // Invoice Items Table
        PdfPTable itemsTable = new PdfPTable(5);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[]{12f, 48f, 12f, 14f, 14f});

        String[] headers = {"Código", "Descripción del Concepto", "Cant.", "Valor Unitario", "Subtotal"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(h, headerCellFont));
            cell.setBackgroundColor(new Color(20, 50, 95));
            cell.setPadding(5);
            itemsTable.addCell(cell);
        }

        PdfPCell codeCell = new PdfPCell(new Paragraph(payment.getConceptCode() != null ? payment.getConceptCode() : "PAGO", normalFont));
        codeCell.setPadding(5);
        itemsTable.addCell(codeCell);

        String desc = (payment.getConceptName() != null ? payment.getConceptName() : "Servicio Educativo") +
                " (Año: " + (payment.getAcademicYear() != null ? payment.getAcademicYear() : 2026) +
                ", Período: " + (payment.getAcademicPeriod() != null ? payment.getAcademicPeriod() : "I") + ")";
        PdfPCell descCell = new PdfPCell(new Paragraph(desc, normalFont));
        descCell.setPadding(5);
        itemsTable.addCell(descCell);

        PdfPCell qtyCell = new PdfPCell(new Paragraph("1", normalFont));
        qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        qtyCell.setPadding(5);
        itemsTable.addCell(qtyCell);

        PdfPCell unitCell = new PdfPCell(new Paragraph(String.format("$%,.2f", subtotal), normalFont));
        unitCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        unitCell.setPadding(5);
        itemsTable.addCell(unitCell);

        PdfPCell totalItemCell = new PdfPCell(new Paragraph(String.format("$%,.2f", subtotal), boldFont));
        totalItemCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalItemCell.setPadding(5);
        itemsTable.addCell(totalItemCell);

        document.add(itemsTable);

        // Totals & Payment Summary Table
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(100);
        totalsTable.setWidths(new float[]{60f, 40f});

        PdfPCell notesCell = new PdfPCell();
        notesCell.setPadding(6);
        notesCell.addElement(new Paragraph("Forma de Pago: " + (payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "Transferencia"), normalFont));
        notesCell.addElement(new Paragraph("Recibo de Soporte: " + (payment.getReceiptNumber() != null ? payment.getReceiptNumber() : "N/A"), normalFont));
        notesCell.addElement(new Paragraph("Tratamiento Tributario: Excluido de IVA conforme al Art. 476 Numeral 6 del Estatuto Tributario Nacional.", smallFont));
        totalsTable.addCell(notesCell);

        PdfPTable subTotals = new PdfPTable(2);
        subTotals.setWidthPercentage(100);
        subTotals.addCell(new PdfPCell(new Paragraph("Subtotal:", normalFont)));
        PdfPCell stv = new PdfPCell(new Paragraph(String.format("$%,.2f COP", subtotal), normalFont));
        stv.setHorizontalAlignment(Element.ALIGN_RIGHT);
        subTotals.addCell(stv);

        subTotals.addCell(new PdfPCell(new Paragraph("IVA (0% Excluido):", normalFont)));
        PdfPCell iv = new PdfPCell(new Paragraph("$0.00 COP", normalFont));
        iv.setHorizontalAlignment(Element.ALIGN_RIGHT);
        subTotals.addCell(iv);

        subTotals.addCell(new PdfPCell(new Paragraph("TOTAL A PAGAR:", boldFont)));
        PdfPCell tv = new PdfPCell(new Paragraph(String.format("$%,.2f COP", total), boldFont));
        tv.setHorizontalAlignment(Element.ALIGN_RIGHT);
        subTotals.addCell(tv);

        PdfPCell rightTotalsCell = new PdfPCell(subTotals);
        rightTotalsCell.setPadding(0);
        totalsTable.addCell(rightTotalsCell);

        document.add(totalsTable);
        document.add(new Paragraph(" "));

        // DIAN Compliance Footer (CUFE + Simulated QR Box)
        PdfPTable dianTable = new PdfPTable(2);
        dianTable.setWidthPercentage(100);
        dianTable.setWidths(new float[]{75f, 25f});

        PdfPCell cufeCell = new PdfPCell();
        cufeCell.setPadding(6);
        cufeCell.addElement(new Paragraph("CÓDIGO ÚNICO DE FACTURA ELECTRÓNICA (CUFE - SHA 384):", subtitleFont));
        cufeCell.addElement(new Paragraph(invoice.getCufe(), smallFont));
        cufeCell.addElement(new Paragraph("Representación gráfica de factura electrónica expedida conforme al Decreto 358 de 2020.", smallFont));
        dianTable.addCell(cufeCell);

        PdfPCell qrCell = new PdfPCell();
        qrCell.setPadding(6);
        qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        qrCell.addElement(new Paragraph("[ CÓDIGO QR DIAN ]", boldFont));
        qrCell.addElement(new Paragraph("Validable en:", smallFont));
        qrCell.addElement(new Paragraph("dian.gov.co", smallFont));
        dianTable.addCell(qrCell);

        document.add(dianTable);

        document.close();
        return file.getAbsolutePath();
    }

    @Override
    public String getFormatName() {
        return "PDF (Documento Visual con OpenPDF)";
    }

    @Override
    public String getFileExtension() {
        return ".pdf";
    }
}
