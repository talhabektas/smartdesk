// src/main/java/com/example/smartdeskbackend/service/impl/SlaTrackingServiceImpl.java
package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.entity.SlaPolicy;
import com.example.smartdeskbackend.entity.SlaTracking;
import com.example.smartdeskbackend.entity.Ticket;
import com.example.smartdeskbackend.exception.ResourceNotFoundException;
import com.example.smartdeskbackend.repository.SlaTrackingRepository;
import com.example.smartdeskbackend.repository.TicketRepository;
import com.example.smartdeskbackend.service.SlaPolicyService;
import com.example.smartdeskbackend.service.SlaTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SlaTrackingServiceImpl implements SlaTrackingService {

    @Autowired
    private SlaTrackingRepository slaTrackingRepository;
    @Autowired
    private SlaPolicyService slaPolicyService;
    @Autowired
    private TicketRepository ticketRepository;

    @Override
    @Transactional
    public SlaTracking createSlaTracking(Ticket ticket) {
        // Zaten bu bilete ait bir SLA takibi var mı kontrol et
        if (slaTrackingRepository.findByTicketId(ticket.getId()).isPresent()) {
            // throw new IllegalArgumentException("SLA tracking already exists for this ticket.");
            // veya mevcut olanı dön
            return slaTrackingRepository.findByTicketId(ticket.getId()).get();
        }

        // Bilete uygun SLA politikasını bul
        Optional<SlaPolicy> policyOpt = slaPolicyService.getApplicableSlaPolicy(
                ticket.getCompany().getId(),
                ticket.getDepartment() != null ? ticket.getDepartment().getId() : null,
                ticket.getPriority()
        );

        if (policyOpt.isPresent()) {
            SlaPolicy policy = policyOpt.get();
            SlaTracking tracking = new SlaTracking();
            tracking.setTicket(ticket);
            tracking.setSlaPolicy(policy);

            // SLA teslim tarihini hesapla
            // Bu hesaplama iş saatleri gibi daha karmaşık kuralları içerebilir.
            // Şimdilik basitçe biletin oluşturulma zamanına çözüm süresi ekleniyor.
            LocalDateTime deadline = ticket.getCreatedAt().plusHours(policy.getResolutionTimeHours());
            // İlk yanıt için de ayrı bir deadline olabilir: ticket.getCreatedAt().plusHours(policy.getFirstResponseTimeHours());

            tracking.setDeadline(deadline);
            tracking.setFirstResponseViolated(false); // Başlangıçta ihlal yok
            tracking.setResolutionViolated(false); // Başlangıçta ihlal yok
            tracking.setEscalated(false);
            tracking.setEscalationLevel(0);
            tracking.setCreatedAt(LocalDateTime.now());
            tracking.setUpdatedAt(LocalDateTime.now());
            return slaTrackingRepository.save(tracking);
        } else {
            // Uygulanabilir bir SLA politikası bulunamadıysa ne yapılmalı?
            // Hata fırlatılabilir veya SLA takibi oluşturulmayabilir.
            System.out.println("No applicable SLA policy found for ticket: " + ticket.getTicketNumber());
            return null;
        }
    }

    @Override
    @Transactional
    public SlaTracking updateSlaTracking(Long id, SlaTracking trackingDetails) {
        SlaTracking tracking = slaTrackingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA Tracking not found with id: " + id));

        tracking.setDeadline(trackingDetails.getDeadline());
        tracking.setFirstResponseViolated(trackingDetails.isFirstResponseViolated());
        tracking.setResolutionViolated(trackingDetails.isResolutionViolated());
        tracking.setEscalated(trackingDetails.isEscalated());
        tracking.setEscalationLevel(trackingDetails.getEscalationLevel());
        tracking.setUpdatedAt(LocalDateTime.now());
        return slaTrackingRepository.save(tracking);
    }

    @Override
    public Optional<SlaTracking> getSlaTrackingByTicketId(Long ticketId) {
        return slaTrackingRepository.findByTicketId(ticketId);
    }

    @Override
    public List<SlaTracking> getFirstResponseViolatedTickets() {
        return slaTrackingRepository.findByFirstResponseViolatedTrueAndEscalatedFalse();
    }

    @Override
    public List<SlaTracking> getResolutionViolatedTickets() {
        return slaTrackingRepository.findByResolutionViolatedTrueAndEscalatedFalse();
    }

    @Override
    @Transactional
    public void escalateSlaViolation(Long trackingId, int newEscalationLevel) {
        SlaTracking tracking = slaTrackingRepository.findById(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException("SLA Tracking not found with id: " + trackingId));
        tracking.setEscalated(true);
        tracking.setEscalationLevel(newEscalationLevel);
        tracking.setUpdatedAt(LocalDateTime.now());
        slaTrackingRepository.save(tracking);
    }

    @Transactional
    public void updateSlaStatusForTicket(Long ticketId, boolean firstResponseDone, boolean resolvedDone) {
        Optional<SlaTracking> trackingOpt = slaTrackingRepository.findByTicketId(ticketId);
        if (trackingOpt.isPresent()) {
            SlaTracking tracking = trackingOpt.get();
            LocalDateTime now = LocalDateTime.now();
            Ticket ticket = tracking.getTicket();

            // İlk yanıt süresi kontrolü
            if (!tracking.isFirstResponseViolated() && firstResponseDone) {
                // İlk yanıt zamanı set edilmişse ve deadline geçmişse
                if (ticket.getFirstResponseAt() != null && tracking.getSlaPolicy() != null) {
                    LocalDateTime firstResponseDeadline = ticket.getCreatedAt().plusHours(tracking.getSlaPolicy().getFirstResponseTimeHours());
                    if (ticket.getFirstResponseAt().isAfter(firstResponseDeadline)) {
                        tracking.setFirstResponseViolated(true);
                    }
                }
            }

            // Çözüm süresi kontrolü
            if (!tracking.isResolutionViolated() && resolvedDone) {
                // Çözüm zamanı set edilmişse ve deadline geçmişse
                if (ticket.getResolvedAt() != null && tracking.getSlaPolicy() != null) {
                    if (ticket.getResolvedAt().isAfter(tracking.getDeadline())) {
                        tracking.setResolutionViolated(true);
                    }
                }
            }
            tracking.setUpdatedAt(now);
            slaTrackingRepository.save(tracking);
        }
    }
}