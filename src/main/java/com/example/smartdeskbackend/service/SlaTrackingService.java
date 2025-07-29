package com.example.smartdeskbackend.service;

import com.example.smartdeskbackend.entity.SlaTracking;
import com.example.smartdeskbackend.entity.Ticket;
import java.util.List;
import java.util.Optional;

public interface SlaTrackingService {
    SlaTracking createSlaTracking(Ticket ticket);
    SlaTracking updateSlaTracking(Long id, SlaTracking trackingDetails);
    Optional<SlaTracking> getSlaTrackingByTicketId(Long ticketId);
    List<SlaTracking> getFirstResponseViolatedTickets();
    List<SlaTracking> getResolutionViolatedTickets();
    void escalateSlaViolation(Long trackingId, int newEscalationLevel);
    void updateSlaStatusForTicket(Long ticketId, boolean firstResponseDone, boolean resolvedDone); // Bu metod da SlaTrackingServiceImpl'de vardı
}