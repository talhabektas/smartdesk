// src/main/java/com/example/smartdeskbackend/scheduler/SlaMonitoringScheduler.java
package com.example.smartdeskbackend.scheduler;

import com.example.smartdeskbackend.entity.Ticket;
import com.example.smartdeskbackend.repository.TicketRepository;
import com.example.smartdeskbackend.integration.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SLA monitoring ve escalation scheduler
 */
@Component
public class SlaMonitoringScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SlaMonitoringScheduler.class);

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Her 15 dakikada bir SLA ihlali kontrolü yapar
     */
    @Scheduled(fixedRate = 900000) // 15 minutes
    public void checkSlaViolations() {
        logger.info("Checking SLA violations...");

        try {
            LocalDateTime now = LocalDateTime.now();

            // SLA ihlali olan ticketları bul
            List<Ticket> violatedTickets = ticketRepository.findSlaViolatedTickets(null, now);

            logger.info("Found {} tickets with SLA violations", violatedTickets.size());

            for (Ticket ticket : violatedTickets) {
                try {
                    // Ticket'ı escalate et
                    ticket.escalate();
                    ticketRepository.save(ticket);

                    // Email notification gönder
                    emailService.sendTicketNotification(ticket, "SLA_VIOLATED");

                    logger.warn("Escalated ticket {} due to SLA violation", ticket.getTicketNumber());

                } catch (Exception e) {
                    logger.error("Error escalating ticket {}: {}", ticket.getTicketNumber(), e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Error in SLA violation check", e);
        }
    }

    /**
     * Her saat başı SLA riski olan ticketları kontrol eder
     */
    @Scheduled(cron = "0 0 * * * ?") // Her saat başı
    public void checkSlaRisks() {
        logger.info("Checking SLA risks...");

        try {
            LocalDateTime riskTime = LocalDateTime.now().plusHours(2);

            // Risk altındaki ticketları bul
            List<Ticket> riskTickets = ticketRepository.findTicketsAtRiskOfSlaViolation(null, riskTime);

            logger.info("Found {} tickets at SLA risk", riskTickets.size());

            for (Ticket ticket : riskTickets) {
                try {
                    // Risk notification gönder
                    emailService.sendTicketNotification(ticket, "SLA_RISK");

                } catch (Exception e) {
                    logger.error("Error sending SLA risk notification for ticket {}: {}",
                            ticket.getTicketNumber(), e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Error in SLA risk check", e);
        }
    }

    /**
     * Günlük SLA raporu (her gece 02:00)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateDailySlaReport() {
        logger.info("Generating daily SLA report...");

        try {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            LocalDateTime today = LocalDateTime.now();

            // Dün oluşturulan ticketları al
            List<Ticket> yesterdayTickets = ticketRepository.findTicketsBetweenDates(null, yesterday, today);

            logger.info("Processing {} tickets for daily SLA report", yesterdayTickets.size());

            // SLA istatistikleri hesapla
            long violatedCount = yesterdayTickets.stream()
                    .filter(Ticket::isSlaBreached)
                    .count();

            long totalTickets = yesterdayTickets.size();
            double slaComplianceRate = totalTickets > 0 ?
                    ((double)(totalTickets - violatedCount) / totalTickets) * 100 : 100.0;

            logger.info("Daily SLA Report - Total: {}, Violated: {}, Compliance: {:.2f}%",
                    totalTickets, violatedCount, slaComplianceRate);

            // Raporu email ile gönder (manager'lara)
            // Bu kısım email template'i ile genişletilebilir

        } catch (Exception e) {
            logger.error("Error generating daily SLA report", e);
        }
    }
}