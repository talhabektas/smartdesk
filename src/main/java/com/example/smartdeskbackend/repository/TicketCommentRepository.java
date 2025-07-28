

// TicketCommentRepository.java
package com.example.smartdeskbackend.repository;

import com.example.smartdeskbackend.entity.TicketComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

    /**
     * Ticket'ın yorumları
     */
    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    /**
     * Public yorumlar (internal olmayan)
     */
    List<TicketComment> findByTicketIdAndIsInternalFalseOrderByCreatedAtAsc(Long ticketId);

    /**
     * Internal yorumlar
     */
    List<TicketComment> findByTicketIdAndIsInternalTrueOrderByCreatedAtAsc(Long ticketId);

    /**
     * Yazar tarafından yazılan yorumlar
     */
    Page<TicketComment> findByAuthorId(Long authorId, Pageable pageable);

    /**
     * Son yorumlar
     */
    @Query("SELECT tc FROM TicketComment tc WHERE tc.ticket.company.id = :companyId " +
            "ORDER BY tc.createdAt DESC")
    Page<TicketComment> findRecentComments(@Param("companyId") Long companyId, Pageable pageable);
}
