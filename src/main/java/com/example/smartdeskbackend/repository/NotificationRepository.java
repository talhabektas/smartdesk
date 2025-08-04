// src/main/java/com/example/smartdeskbackend/repository/NotificationRepository.java
package com.example.smartdeskbackend.repository;

import com.example.smartdeskbackend.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientUserId(Long userId, Pageable pageable);

    List<Notification> findByRecipientUserIdAndIsReadFalse(Long userId);

    long countByRecipientUserIdAndIsReadFalse(Long userId);
}