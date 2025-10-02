package com.project.thelittlethings.repositories;

import com.project.thelittlethings.entities.NotificationPrefs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPrefsRepository extends JpaRepository<NotificationPrefs, Long> {
}
