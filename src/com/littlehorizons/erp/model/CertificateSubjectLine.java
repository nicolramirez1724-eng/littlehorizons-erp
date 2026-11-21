package com.littlehorizons.erp.model;

import java.math.BigDecimal;
import java.util.Map;

public record CertificateSubjectLine(String subjectCode, String subjectName,
                                     Map<String, BigDecimal> gradesByPeriod, BigDecimal average) {
}
