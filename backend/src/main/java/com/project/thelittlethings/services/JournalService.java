package com.project.thelittlethings.services;

import com.project.thelittlethings.entities.Journal;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.entities.Win;
import com.project.thelittlethings.dto.journals.CreateJournalRequest;
import com.project.thelittlethings.dto.journals.UpdateJournalRequest;
import com.project.thelittlethings.dto.journals.JournalResponse;
import com.project.thelittlethings.repositories.JournalRepository;
import com.project.thelittlethings.repositories.UserRepository;
import com.project.thelittlethings.repositories.WinRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class JournalService {
    
    private final JournalRepository journalRepository;
    private final UserRepository userRepository;
    private final WinRepository winRepository;

    public JournalService(JournalRepository journalRepository, UserRepository userRepository, WinRepository winRepository) {
        this.journalRepository = journalRepository;
        this.userRepository = userRepository;
        this.winRepository = winRepository;
    }

    @Transactional
    public JournalResponse createJournal(Long userId, CreateJournalRequest request) {
        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) throw new IllegalArgumentException("User not found");
        User user = userOpt.get();

        Journal journal = new Journal();
        journal.setUser(user);
        journal.setTitle(request.getTitle());
        journal.setContent(request.getContent());

        if (request.getLinkedWinId() != null) {
            Optional<Win> win = winRepository.findById(request.getLinkedWinId());
            if (win.isPresent() && win.get().getUser().getUserId().equals(userId)) {
                journal.setLinkedWin(win.get());
            } else {
                throw new IllegalArgumentException("Win not found");
            }
        }

        Journal saved = journalRepository.save(journal);
        return JournalResponse.fromJournal(saved);
    }

    public JournalResponse getJournal(Long journalId, Long userId) {
        var journalOpt = journalRepository.findByJournalIdAndUser_UserId(journalId, userId);
        if (journalOpt.isEmpty()) throw new IllegalArgumentException("Journal not found");
        return JournalResponse.fromJournal(journalOpt.get());
    }

    public List<JournalResponse> getAllJournals(Long userId, String sortBy) {
        List<Journal> journals;
        
        if ("title".equalsIgnoreCase(sortBy)) {
            journals = journalRepository.findByUser_UserIdOrderByTitleAsc(userId);
        } else {
            // Default to date sorting (newest first)
            journals = journalRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
        }

        List<JournalResponse> result = new ArrayList<>();
        for (Journal j : journals) {
            result.add(JournalResponse.fromJournal(j));
        }
        return result;
    }

    @Transactional
    public JournalResponse updateJournal(Long journalId, Long userId, UpdateJournalRequest request) {
        var journalOpt = journalRepository.findByJournalIdAndUser_UserId(journalId, userId);
        if (journalOpt.isEmpty()) throw new IllegalArgumentException("Journal not found");
        Journal journal = journalOpt.get();

        journal.setTitle(request.getTitle());
        journal.setContent(request.getContent());

        if (request.getLinkedWinId() != null) {
            Optional<Win> win = winRepository.findById(request.getLinkedWinId());
            if (win.isPresent() && win.get().getUser().getUserId().equals(userId)) {
                journal.setLinkedWin(win.get());
            } else {
                throw new IllegalArgumentException("Win not found");
            }
        }

        Journal savedJournal = journalRepository.save(journal);
        return JournalResponse.fromJournal(savedJournal);
    }

    @Transactional
    public boolean deleteJournal(Long journalId, Long userId) {
        if (!journalRepository.existsByJournalIdAndUser_UserId(journalId, userId)) {
            return false;
        }
        
        journalRepository.deleteById(journalId);
        return true;
    }

    public List<Win> getUserWins(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }
        return winRepository.findByUser_UserId(userId);
    }
}
