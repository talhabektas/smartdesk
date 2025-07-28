
// TicketHistoryRepository.java
package com.example.smartdeskbackend.repository;

import com.example.smartdeskbackend.entity.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {

    /**
     * Ticket'ın geçmişi
     */
    List<TicketHistory> findByTicketIdOrderByCreatedAtDesc(Long ticketId);

    /**
     * Kullanıcının yaptığı değişiklikler
     */
    List<TicketHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Belirli alan değişiklikleri
     */
    List<TicketHistory> findByTicketIdAndFieldNameOrderByCreatedAtDesc(Long ticketId, String fieldName);
}