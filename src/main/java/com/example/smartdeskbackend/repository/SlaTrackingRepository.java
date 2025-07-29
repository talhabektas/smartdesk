package com.example.smartdeskbackend.repository;

import com.example.smartdeskbackend.entity.SlaTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlaTrackingRepository extends JpaRepository<SlaTracking, Long> {
    Optional<SlaTracking> findByTicketId(Long ticketId);
    List<SlaTracking> findByFirstResponseViolatedTrueAndEscalatedFalse();
    List<SlaTracking> findByResolutionViolatedTrueAndEscalatedFalse();
}