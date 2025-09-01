package com.project.thelittlethings.repositories;

import com.project.thelittlethings.entities.Journal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JournalRepository extends JpaRepository<Journal, Long> {

    // Find a specific journal entry owned by a user
    Optional<Journal> findByJournalIdAndUser_UserId(Long journalId, Long userId);

    // Check if a user has any journal entries
    boolean existsByUser_UserId(Long userId);

    // Verify ownership: does this journal entry belong to the given user?
    boolean existsByJournalIdAndUser_UserId(Long journalId, Long userId);
}
