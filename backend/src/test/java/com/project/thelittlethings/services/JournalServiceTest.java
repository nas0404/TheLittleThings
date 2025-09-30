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

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JournalServiceTest {

    @Mock JournalRepository journalRepo;
    @Mock UserRepository userRepo;
    @Mock WinRepository winRepo;

    JournalService service;

    User u1;
    Journal j1;
    Win w1;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new JournalService(journalRepo, userRepo, winRepo);
        
        u1 = new User();
        u1.setUserId(42L);

        w1 = new Win();
        w1.setWinId(123L);
        w1.setUser(u1);

        j1 = new Journal();
        j1.setJournalId(456L);
        j1.setTitle("My coding journey");
        j1.setContent("Today I learned about testing...");
        j1.setUser(u1);
    }

    // helper method I made
    private User makeUser(long id) {
        var user = new User();
        user.setUserId(id);
        return user;
    }

    @Test
    void testCreateJournal() {
        when(userRepo.findById(42L)).thenReturn(Optional.of(u1));
        when(journalRepo.save(any(Journal.class))).thenReturn(j1);

        var req = new CreateJournalRequest();
        req.setTitle("New entry");
        req.setContent("Some content here");

        JournalResponse result = service.createJournal(42L, req);

        assertNotNull(result);
        verify(journalRepo).save(any(Journal.class));
    }

    @Test 
    void testCreateJournalWithInvalidUser() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        var req = new CreateJournalRequest();
        req.setTitle("test");
        req.setContent("test content");

        assertThrows(IllegalArgumentException.class, 
            () -> service.createJournal(99L, req));
    }

    @Test
    void testGetJournal() {
        when(journalRepo.findByJournalIdAndUser_UserId(456L, 42L))
            .thenReturn(Optional.of(j1));

        var result = service.getJournal(456L, 42L);

        assertNotNull(result);
    }

    @Test
    void testGetJournalNotFound() {
        when(journalRepo.findByJournalIdAndUser_UserId(999L, 42L))
            .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, 
            () -> service.getJournal(999L, 42L));
    }

    @Test
    void testGetAllJournals() {
        List<Journal> journals = Arrays.asList(j1);
        when(journalRepo.findByUser_UserIdOrderByCreatedAtDesc(42L))
            .thenReturn(journals);

        List<JournalResponse> result = service.getAllJournals(42L, null);

        assertEquals(1, result.size());
    }

    @Test
    void testUpdateJournal() {
        when(journalRepo.findByJournalIdAndUser_UserId(456L, 42L))
            .thenReturn(Optional.of(j1));
        when(journalRepo.save(any(Journal.class))).thenReturn(j1);

        var updateReq = new UpdateJournalRequest();
        updateReq.setTitle("Updated title");
        updateReq.setContent("Updated content");

        JournalResponse result = service.updateJournal(456L, 42L, updateReq);

        assertNotNull(result);
        verify(journalRepo).save(j1);
    }

    @Test
    void testDeleteJournal() {
        when(journalRepo.existsByJournalIdAndUser_UserId(456L, 42L)).thenReturn(true);

        boolean result = service.deleteJournal(456L, 42L);

        assertTrue(result);
        verify(journalRepo).deleteById(456L);
    }
}