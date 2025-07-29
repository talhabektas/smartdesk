package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.repository.TicketRepository;
import com.example.smartdeskbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("ticketSecurityService")
public class TicketSecurityService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Kullanıcının ticket'a erişim hakkı olup olmadığını kontrol eder
     */
    public boolean hasAccessToTicket(Long ticketId, Long userId) {
        try {
            // Super admin her ticket'a erişebilir
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getAuthorities().stream()
                    .anyMatch(grantedAuthority ->
                            grantedAuthority.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
                return true;
            }

            return ticketRepository.findById(ticketId)
                    .flatMap(ticket -> userRepository.findById(userId)
                            .map(user -> {
                                // Aynı şirket kontrolü
                                if (!ticket.getCompany().getId().equals(user.getCompany().getId())) {
                                    return false;
                                }

                                // Role-based erişim kontrolü
                                switch (user.getRole()) {
                                    case MANAGER:
                                    case AGENT:
                                        return true;
                                    case CUSTOMER:
                                        // Customer sadece kendi ticket'larını görebilir
                                        return ticket.getCustomer() != null &&
                                                ticket.getCustomer().getEmail().equals(user.getEmail());
                                    default:
                                        return false;
                                }
                            }))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Kullanıcının ticket'ın customer'ı olup olmadığını kontrol eder
     */
    public boolean isTicketCustomer(Long ticketId, Long userId) {
        try {
            return ticketRepository.findById(ticketId)
                    .flatMap(ticket -> userRepository.findById(userId)
                            .map(user -> ticket.getCustomer() != null &&
                                    ticket.getCustomer().getEmail().equals(user.getEmail())))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }
}