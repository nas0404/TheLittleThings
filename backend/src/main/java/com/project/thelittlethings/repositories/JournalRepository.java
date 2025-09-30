package com.project.thelittlethings.repositories;

import com.project.thelittlethings.entities.Journal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JournalRepository extends JpaRepository<Journal, Long> {

    
    Optional<Journal> findByJournalIdAndUser_UserId(Long journalId, Long userId);
    @Query("SELECT j FROM Journal j WHERE j.user.userId = :userId ORDER BY j.createdAt DESC")
    List<Journal> findByUser_UserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT j FROM Journal j WHERE j.user.userId = :userId ORDER BY j.title ASC")
    List<Journal> findByUser_UserIdOrderByTitleAsc(@Param("userId") Long userId);

    List<Journal> findByUser_UserId(Long userId);

    boolean existsByUser_UserId(Long userId);

    boolean existsByJournalIdAndUser_UserId(Long journalId, Long userId);
}
