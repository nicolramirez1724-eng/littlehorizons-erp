package com.littlehorizons.erp.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Generates the simulated CUFE (Código Único de Factura Electrónica)
 * according to the DIAN technical standard using SHA-384 hashing.
 */
public class CufeGenerator {

    private static final String DEFAULT_TECHNICAL_KEY = "LittleHorizons2026DIANClaveTecnicaSegura#";
    private static final String NIT_EMISOR = "901458732";
    private static final String TIPO_AMBIENTE = "1"; // 1 = Producción / Simulado Oficial

    private CufeGenerator() {
        // Utility class
    }

    /**
     * Generates a 96-character hexadecimal SHA-384 CUFE string based on DIAN specifications.
     *
     * @param invoiceNumber Invoice number (e.g. FE-000101)
     * @param issueDate Issue date (YYYY-MM-DD)
     * @param issueTime Issue time (HH:mm:ss)
     * @param subtotal Subtotal amount before tax
     * @param totalTax Total IVA/tax amount
     * @param totalAmount Total invoice amount
     * @param customerDocNumber Acudiente identification number
     * @return 96-character hex string representing the CUFE
     */
    public static String generateCufe(String invoiceNumber,
                                      String issueDate,
                                      String issueTime,
                                      double subtotal,
                                      double totalTax,
                                      double totalAmount,
                                      String customerDocNumber) {
        String cleanInvoiceNum = invoiceNumber != null ? invoiceNumber.trim() : "FE-000000";
        String cleanDate = issueDate != null ? issueDate.trim() : "2026-01-01";
        String cleanTime = issueTime != null ? issueTime.trim() : "00:00:00";
        String formattedSubtotal = String.format(java.util.Locale.US, "%.2f", subtotal);
        String formattedTax = String.format(java.util.Locale.US, "%.2f", totalTax);
        String formattedTotal = String.format(java.util.Locale.US, "%.2f", totalAmount);
        String cleanDoc = customerDocNumber != null ? customerDocNumber.trim() : "222222222222";

        // Formula: NumFac + FecFac + HorFac + ValFac + CodImp1 + ValImp1 + ValTot + NitOBL + NumAdq + ClaveTecnica + TipoAmbiente
        String rawString = cleanInvoiceNum +
                cleanDate +
                cleanTime +
                formattedSubtotal +
                "01" + // 01 = IVA
                formattedTax +
                formattedTotal +
                NIT_EMISOR +
                cleanDoc +
                DEFAULT_TECHNICAL_KEY +
                TIPO_AMBIENTE;

        return calculateSha384(rawString);
    }

    private static String calculateSha384(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-384");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            LoggerManager.logError("SHA-384 algorithm not available", e);
            // Fallback mock CUFE
            return Integer.toHexString(text.hashCode()) + "000000000000000000000000000000000000000000000000";
        }
    }
}
