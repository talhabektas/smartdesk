// src/main/java/com/example/smartdeskbackend/service/CustomerSatisfactionService.java
package com.example.smartdeskbackend.service;

import com.example.smartdeskbackend.entity.Ticket; // Ticket entity'si kullanıldığı için import edildi

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional; // Optional kullanılıyorsa import edilir

public interface CustomerSatisfactionService {
    // CustomerSatisfactionServiceImpl içinde override edilen tüm metod imzaları buraya eklendi
    void addSatisfactionRating(Long ticketId, int rating);
    Double getAverageCustomerSatisfaction(Long companyId, LocalDate startDate);
    Map<Integer, Long> getSatisfactionDistribution(Long companyId, LocalDate startDate, LocalDate endDate);
    List<Ticket> getTicketsBySatisfactionRating(Long companyId, int rating, LocalDate startDate, LocalDate endDate);
}