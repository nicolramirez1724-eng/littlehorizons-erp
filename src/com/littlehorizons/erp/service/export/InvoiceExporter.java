package com.littlehorizons.erp.service.export;

import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;

/**
 * Strategy interface for multi-format invoice export (PDF, TXT, XML).
 */
public interface InvoiceExporter {

    /**
     * Exports the invoice to the respective format on disk.
     *
     * @param invoice Invoice entity with metadata
     * @param payment Complete payment entity with financial details
     * @return Generated file path
     * @throws Exception if generation fails
     */
    String export(Invoice invoice, Payment payment) throws Exception;

    /**
     * Human-readable name of the format (e.g. "PDF", "TXT", "XML").
     */
    String getFormatName();

    /**
     * File extension (e.g. ".pdf", ".txt", ".xml").
     */
    String getFileExtension();
}
