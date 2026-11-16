package com.littlehorizons.erp.service.export;

import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.util.LoggerManager;

import java.util.*;

/**
 * Orchestrator for multi-format invoice generation.
 * Allows exporting in PDF, TXT, XML, or ALL formats simultaneously.
 */
public class InvoiceExportManager {

    private final Map<String, InvoiceExporter> exporters = new LinkedHashMap<>();

    public InvoiceExportManager() {
        exporters.put("PDF", new PdfInvoiceExporter());
        exporters.put("TXT", new TxtInvoiceExporter());
        exporters.put("XML", new XmlInvoiceExporter());
    }

    /**
     * Exports an invoice using a single chosen exporter format.
     */
    public String exportSingle(String formatKey, Invoice invoice, Payment payment) throws Exception {
        InvoiceExporter exporter = exporters.get(formatKey.toUpperCase());
        if (exporter == null) {
            throw new IllegalArgumentException("Unsupported invoice export format: " + formatKey);
        }
        return exporter.export(invoice, payment);
    }

    /**
     * Exports the invoice in ALL configured formats (PDF, TXT, XML) simultaneously.
     *
     * @return Map of Format -> FilePath
     */
    public Map<String, String> exportAll(Invoice invoice, Payment payment) {
        Map<String, String> results = new LinkedHashMap<>();
        for (Map.Entry<String, InvoiceExporter> entry : exporters.entrySet()) {
            try {
                String path = entry.getValue().export(invoice, payment);
                results.put(entry.getKey(), path);
            } catch (Exception e) {
                LoggerManager.logError("Failed to export invoice " + invoice.getInvoiceNumber() + " in format " + entry.getKey(), e);
                results.put(entry.getKey(), "ERROR: " + e.getMessage());
            }
        }
        return results;
    }

    public Set<String> getAvailableFormats() {
        return Collections.unmodifiableSet(exporters.keySet());
    }
}
