package com.athena.cases.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.athena.cases.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
