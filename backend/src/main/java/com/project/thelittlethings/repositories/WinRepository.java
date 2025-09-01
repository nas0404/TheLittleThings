package com.project.thelittlethings.repositories;


import com.project.thelittlethings.entities.Win;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WinRepository extends JpaRepository<Win, Long> {

    // Listing
    List<Win> findByUserId(Long userId);          // all wins for a user
    List<Win> findByGoalId(Long goalId);          // all wins tied to a goal

    //  Safe ownership fetch
    Optional<Win> findByWinIdAndUserId(Long winId, Long userId);

    //  Journal 1:1 link (wins.journal_id is unique)
    Optional<Win> findByJournalId(Long journalId);

    //  Boolean checks
    boolean existsByWinIdAndUserId(Long winId, Long userId);   // ownership check
    boolean existsByUserId(Long userId);                       // any wins for user?
    boolean existsByGoalId(Long goalId);                       // any wins for goal?
    boolean existsByJournalId(Long journalId);              // already linked to a journal?

    //  Optional counters
    long countByUserId(Long userId);
    long countByGoalId(Long goalId);
}

