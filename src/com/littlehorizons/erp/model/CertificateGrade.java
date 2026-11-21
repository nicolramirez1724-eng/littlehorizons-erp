package com.littlehorizons.erp.model;

import java.math.BigDecimal;

public record CertificateGrade(int subjectId, String subjectCode, String subjectName, String period, BigDecimal value) {
}
