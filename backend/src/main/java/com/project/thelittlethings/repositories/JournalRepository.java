package com.project.thelittlethings.repositories;

import com.project.thelittlethings.entities.Journal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JournalRepository extends JpaRepository<Journal, Long> {

    // Find a specific journal entry owned by a user
    Optional<Journal> findByJournalIdAndUser_UserId(Long journalId, Long userId);

    // Find all journal entries for a user, sorted by date (newest first)
    @Query("SELECT j FROM Journal j WHERE j.user.userId = :userId ORDER BY j.createdAt DESC")
    List<Journal> findByUser_UserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // Find all journal entries for a user, sorted alphabetically by title
    @Query("SELECT j FROM Journal j WHERE j.user.userId = :userId ORDER BY j.title ASC")
    List<Journal> findByUser_UserIdOrderByTitleAsc(@Param("userId") Long userId);

    // Find all journal entries for a user (no specific sort)
    List<Journal> findByUser_UserId(Long userId);

    // Check if a user has any journal entries
    boolean existsByUser_UserId(Long userId);

    // Verify ownership: does this journal entry belong to the given user?
    boolean existsByJournalIdAndUser_UserId(Long journalId, Long userId);
}
