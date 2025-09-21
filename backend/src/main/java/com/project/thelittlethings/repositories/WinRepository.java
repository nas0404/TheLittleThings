package com.project.thelittlethings.repositories;


import com.project.thelittlethings.entities.Win;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WinRepository extends JpaRepository<Win, Long> {

    // Listing
    List<Win> findByUser_UserId(Long userId);          // all wins for a user
    List<Win> findByGoal_GoalId(Long goalId);          // all wins tied to a goal

    //  Safe ownership fetch
    Optional<Win> findByWinIdAndUser_UserId(Long winId, Long userId);

    //  Journal 1:1 link (wins.journal_id is unique)
    Optional<Win> findByJournalId(Long journalId);

    //  Boolean checks
    boolean existsByWinIdAndUser_UserId(Long winId, Long userId);   // ownership check
    boolean existsByUser_UserId(Long userId);                       // any wins for user?
    boolean existsByGoal_GoalId(Long goalId);                       // any wins for goal?
    boolean existsByJournalId(Long journalId);              // already linked to a journal?

    //  Optional counters
    long countByUser_UserId(Long userId);
    long countByGoal_GoalId(Long goalId);

    //count recent wins for a user
    @Query("SELECT COUNT(w) FROM Win w WHERE w.user.userId = :userId AND w.completionDate >= :after")
    long countRecentWinsForUser(@Param("userId") Long userId, @Param("after") java.time.OffsetDateTime after);
}

