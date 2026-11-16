package com.littlehorizons.erp.service.export;

import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Exports invoices in standard DIAN MUISCA / UBL 2.1 XML format.
 */
public class XmlInvoiceExporter implements InvoiceExporter {

    @Override
    public String export(Invoice invoice, Payment payment) throws Exception {
        File dir = new File("invoices");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = String.format("factura_%s.xml", invoice.getInvoiceNumber());
        File file = new File(dir, fileName);

        double subtotal = payment.getPaidAmount();
        double iva = 0.00;
        double total = subtotal + iva;

        try (PrintWriter pw = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
            pw.println("<Invoice xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\"");
            pw.println("         xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\"");
            pw.println("         xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\"");
            pw.println("         xmlns:fe=\"http://www.dian.gov.co/contratos/facturaelectronica/v1\">");
            pw.println("    <cbc:UBLVersionID>UBL 2.1</cbc:UBLVersionID>");
            pw.println("    <cbc:CustomizationID>10</cbc:CustomizationID>");
            pw.println("    <cbc:ProfileID>DIAN 2.1: Factura Electronica de Venta</cbc:ProfileID>");
            pw.println("    <cbc:ID>" + invoice.getInvoiceNumber() + "</cbc:ID>");
            pw.println("    <cbc:UUID schemeName=\"CUFE-SHA384\">" + invoice.getCufe() + "</cbc:UUID>");
            pw.println("    <cbc:IssueDate>" + invoice.getIssueDate() + "</cbc:IssueDate>");
            pw.println("    <cbc:IssueTime>12:00:00-05:00</cbc:IssueTime>");
            pw.println("    <cbc:InvoiceTypeCode>01</cbc:InvoiceTypeCode>");
            pw.println("    <cbc:DocumentCurrencyCode>COP</cbc:DocumentCurrencyCode>");

            // Supplier Party (School)
            pw.println("    <cac:AccountingSupplierParty>");
            pw.println("        <cac:Party>");
            pw.println("            <cac:PartyIdentification>");
            pw.println("                <cbc:ID schemeAgencyID=\"195\" schemeID=\"4\" schemeName=\"31\">901458732</cbc:ID>");
            pw.println("            </cac:PartyIdentification>");
            pw.println("            <cac:PartyName>");
            pw.println("                <cbc:Name>Colegio Internacional Little Horizons S.A.S.</cbc:Name>");
            pw.println("            </cac:PartyName>");
            pw.println("            <cac:PhysicalLocation>");
            pw.println("                <cac:Address>");
            pw.println("                    <cbc:CityName>Bogota D.C.</cbc:CityName>");
            pw.println("                    <cbc:CountrySubentity>Cundinamarca</cbc:CountrySubentity>");
            pw.println("                    <cac:AddressLine>");
            pw.println("                        <cbc:Line>Calle 127 # 15-45</cbc:Line>");
            pw.println("                    </cac:AddressLine>");
            pw.println("                </cac:Address>");
            pw.println("            </cac:PhysicalLocation>");
            pw.println("        </cac:Party>");
            pw.println("    </cac:AccountingSupplierParty>");

            // Customer Party (Guardian)
            pw.println("    <cac:AccountingCustomerParty>");
            pw.println("        <cac:Party>");
            pw.println("            <cac:PartyIdentification>");
            pw.println("                <cbc:ID schemeAgencyID=\"195\" schemeName=\"13\">" +
                    (payment.getGuardianDocNumber() != null ? payment.getGuardianDocNumber() : "222222222222") + "</cbc:ID>");
            pw.println("            </cac:PartyIdentification>");
            pw.println("            <cac:PartyName>");
            pw.println("                <cbc:Name>" + (payment.getGuardianName() != null ? payment.getGuardianName() : "Acudiente Responsable") + "</cbc:Name>");
            pw.println("            </cac:PartyName>");
            pw.println("            <cac:Contact>");
            pw.println("                <cbc:Telephone>" + (payment.getGuardianPhone() != null ? payment.getGuardianPhone() : "N/A") + "</cbc:Telephone>");
            pw.println("            </cac:Contact>");
            pw.println("        </cac:Party>");
            pw.println("    </cac:AccountingCustomerParty>");

            // Payment Means
            pw.println("    <cac:PaymentMeans>");
            pw.println("        <cbc:PaymentMeansCode>10</cbc:PaymentMeansCode>");
            pw.println("        <cbc:PaymentID>" + (payment.getReceiptNumber() != null ? payment.getReceiptNumber() : "N/A") + "</cbc:PaymentID>");
            pw.println("    </cac:PaymentMeans>");

            // Tax Total (Exempt / Excluded per Art. 476 Num 6 ET)
            pw.println("    <cac:TaxTotal>");
            pw.printf("        <cbc:TaxAmount currencyID=\"COP\">%.2f</cbc:TaxAmount>%n", iva);
            pw.println("        <cac:TaxSubtotal>");
            pw.printf("            <cbc:TaxableAmount currencyID=\"COP\">%.2f</cbc:TaxableAmount>%n", subtotal);
            pw.printf("            <cbc:TaxAmount currencyID=\"COP\">%.2f</cbc:TaxAmount>%n", iva);
            pw.println("            <cac:TaxCategory>");
            pw.println("                <cbc:Percent>0.00</cbc:Percent>");
            pw.println("                <cac:TaxScheme>");
            pw.println("                    <cbc:ID>01</cbc:ID>");
            pw.println("                    <cbc:Name>IVA - Excluido Art. 476 Num 6 E.T.</cbc:Name>");
            pw.println("                </cac:TaxScheme>");
            pw.println("            </cac:TaxCategory>");
            pw.println("        </cac:TaxSubtotal>");
            pw.println("    </cac:TaxTotal>");

            // Legal Monetary Total
            pw.println("    <cac:LegalMonetaryTotal>");
            pw.printf("        <cbc:LineExtensionAmount currencyID=\"COP\">%.2f</cbc:LineExtensionAmount>%n", subtotal);
            pw.printf("        <cbc:TaxExclusiveAmount currencyID=\"COP\">%.2f</cbc:TaxExclusiveAmount>%n", subtotal);
            pw.printf("        <cbc:TaxInclusiveAmount currencyID=\"COP\">%.2f</cbc:TaxInclusiveAmount>%n", total);
            pw.printf("        <cbc:PayableAmount currencyID=\"COP\">%.2f</cbc:PayableAmount>%n", total);
            pw.println("    </cac:LegalMonetaryTotal>");

            // Line Item
            pw.println("    <cac:InvoiceLine>");
            pw.println("        <cbc:ID>1</cbc:ID>");
            pw.println("        <cbc:InvoicedQuantity unitCode=\"EA\">1.00</cbc:InvoicedQuantity>");
            pw.printf("        <cbc:LineExtensionAmount currencyID=\"COP\">%.2f</cbc:LineExtensionAmount>%n", subtotal);
            pw.println("        <cac:Item>");
            pw.println("            <cbc:Description>" + (payment.getConceptName() != null ? payment.getConceptName() : "Servicio Educativo") + " - Estudiante: " + (payment.getStudentName() != null ? payment.getStudentName() : "Beneficiario") + "</cbc:Description>");
            pw.println("        </cac:Item>");
            pw.println("        <cac:Price>");
            pw.printf("            <cbc:PriceAmount currencyID=\"COP\">%.2f</cbc:PriceAmount>%n", subtotal);
            pw.println("        </cac:Price>");
            pw.println("    </cac:InvoiceLine>");

            pw.println("</Invoice>");
        }

        return file.getAbsolutePath();
    }

    @Override
    public String getFormatName() {
        return "XML (Estándar UBL 2.1 / MUISCA DIAN)";
    }

    @Override
    public String getFileExtension() {
        return ".xml";
    }
}
