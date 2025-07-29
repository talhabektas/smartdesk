// src/main/java/com/example/smartdeskbackend/service/impl/CustomerSatisfactionServiceImpl.java
package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.entity.Ticket;
import com.example.smartdeskbackend.exception.ResourceNotFoundException;
import com.example.smartdeskbackend.repository.TicketRepository;
import com.example.smartdeskbackend.service.CustomerSatisfactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CustomerSatisfactionServiceImpl implements CustomerSatisfactionService {

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    @Transactional
    public void addSatisfactionRating(Long ticketId, int rating) {
        // Derecelendirme 1-5 arasında mı kontrolü
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Customer satisfaction rating must be between 1 and 5.");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        // Sadece çözülmüş veya kapalı biletler derecelendirilebilir
        if (!ticket.getStatus().toString().equals("RESOLVED") && !ticket.getStatus().toString().equals("CLOSED")) {
            throw new IllegalArgumentException("Only resolved or closed tickets can be rated.");
        }

        ticket.setCustomerSatisfactionRating(rating);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
    }

    @Override
    public Double getAverageCustomerSatisfaction(Long companyId, LocalDate startDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        return ticketRepository.getAverageCustomerSatisfaction(companyId, startDateTime);
    }

    @Override
    public Map<Integer, Long> getSatisfactionDistribution(Long companyId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        List<Ticket> tickets = ticketRepository.findTicketsBetweenDates(companyId, startDateTime, endDateTime);

        // Sadece derecelendirilmiş biletleri dahil et ve dağılımı hesapla
        return tickets.stream()
                .filter(t -> t.getCustomerSatisfactionRating() != null)
                .collect(Collectors.groupingBy(Ticket::getCustomerSatisfactionRating, Collectors.counting()));
    }

    @Override
    public List<Ticket> getTicketsBySatisfactionRating(Long companyId, int rating, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        // Bu filtreleme için TicketRepository'de doğrudan bir sorgu yok.
        // Bu yüzden mevcut findTicketsBetweenDates metodu kullanılarak stream ile filtreleme yapılır.
        // Daha iyi performans için TicketRepository'ye özel bir sorgu (@Query) eklenebilir.
        // Örnek özel sorgu: findByCompanyIdAndCustomerSatisfactionRatingAndCreatedAtBetween(...)
        return ticketRepository.findTicketsBetweenDates(companyId, startDateTime, endDateTime).stream()
                .filter(t -> t.getCustomerSatisfactionRating() != null && t.getCustomerSatisfactionRating() == rating)
                .collect(Collectors.toList());
    }
}