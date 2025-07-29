package com.example.smartdeskbackend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportService {
    Map<String, Object> getDailyTicketSummary(Long companyId, LocalDate date);
    List<Object[]> getAgentPerformanceReport(Long companyId, LocalDate startDate, LocalDate endDate);
    byte[] generatePdfReport(Long companyId, String reportType, LocalDate startDate, LocalDate endDate);
    byte[] generateExcelReport(Long companyId, String reportType, LocalDate startDate, LocalDate endDate);
}