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
    void testCreateBasicJournal() {
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
    void createWithWin_works() {
        var request = new CreateJournalRequest();
        request.setTitle("Win journal");
        request.setContent("I achieved something!");
        request.setLinkedWinId(123L);
        
        when(userRepo.findById(42L)).thenReturn(Optional.of(u1));
        when(winRepo.findById(123L)).thenReturn(Optional.of(w1));
        when(journalRepo.save(any(Journal.class))).thenReturn(j1);

        var result = service.createJournal(42L, request);

        assertNotNull(result);
        verify(winRepo).findById(123L);
        verify(journalRepo).save(any(Journal.class));
    }

    @Test 
    void createJournal_noUser_throwsError() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        var req = new CreateJournalRequest();
        req.setTitle("test");
        req.setContent("test content");

        var ex = assertThrows(IllegalArgumentException.class, 
            () -> service.createJournal(99L, req));
    }

    @Test
    void createJournal_winNotFound_fails() {
        var request = new CreateJournalRequest();
        request.setTitle("Journal with missing win");
        request.setContent("content");
        request.setLinkedWinId(999L);
        
        when(userRepo.findById(42L)).thenReturn(Optional.of(u1));
        when(winRepo.findById(999L)).thenReturn(Optional.empty());

        Exception thrown = assertThrows(IllegalArgumentException.class, 
            () -> service.createJournal(42L, request));
        
        assertTrue(thrown.getMessage() != null);
    }

    @Test
    void createJournal_winBelongsToSomeoneElse() {
        User otherGuy = makeUser(777L);
        w1.setUser(otherGuy);
        
        var req = new CreateJournalRequest();
        req.setTitle("Trying to use someone else's win");
        req.setContent("This should fail");
        req.setLinkedWinId(123L);
        
        when(userRepo.findById(42L)).thenReturn(Optional.of(u1));
        when(winRepo.findById(123L)).thenReturn(Optional.of(w1));

        assertThrows(IllegalArgumentException.class, 
            () -> service.createJournal(42L, req));
    }

    @Test
    void getJournal_basic() {
        when(journalRepo.findByJournalIdAndUser_UserId(456L, 42L))
            .thenReturn(Optional.of(j1));

        var result = service.getJournal(456L, 42L);

        assertNotNull(result);
    }

    @Test
    void getJournal_notFound() {
        when(journalRepo.findByJournalIdAndUser_UserId(999L, 42L))
            .thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class, 
            () -> service.getJournal(999L, 42L));
    }

    @Test
    void getAllJournals_defaultSorting() {
        List<Journal> journals = Arrays.asList(j1);
        when(journalRepo.findByUser_UserIdOrderByCreatedAtDesc(42L))
            .thenReturn(journals);

        List<JournalResponse> result = service.getAllJournals(42L, null);

        assertEquals(1, result.size());
        verify(journalRepo).findByUser_UserIdOrderByCreatedAtDesc(42L);
    }

    @Test
    void getAllJournals_sortByTitle() {
        List<Journal> journals = Arrays.asList(j1);
        when(journalRepo.findByUser_UserIdOrderByTitleAsc(42L)).thenReturn(journals);

        var result = service.getAllJournals(42L, "title");

        assertNotNull(result);
        assertTrue(result.size() == 1);
        verify(journalRepo).findByUser_UserIdOrderByTitleAsc(42L);
    }

    @Test
    void updateJournal_success() {
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
    void updateJournal_withWin() {
        var updateRequest = new UpdateJournalRequest();
        updateRequest.setTitle("Updated with win");
        updateRequest.setContent("New content");
        updateRequest.setLinkedWinId(123L);
        
        when(journalRepo.findByJournalIdAndUser_UserId(456L, 42L)).thenReturn(Optional.of(j1));
        when(winRepo.findById(123L)).thenReturn(Optional.of(w1));
        when(journalRepo.save(any(Journal.class))).thenReturn(j1);

        var result = service.updateJournal(456L, 42L, updateRequest);

        assertNotNull(result);
        verify(winRepo).findById(123L);
        verify(journalRepo).save(j1);
    }

    @Test
    void updateJournal_journalNotFound() {
        when(journalRepo.findByJournalIdAndUser_UserId(999L, 42L)).thenReturn(Optional.empty());

        var updateReq = new UpdateJournalRequest();
        updateReq.setTitle("Won't work");

        assertThrows(IllegalArgumentException.class, 
            () -> service.updateJournal(999L, 42L, updateReq));
    }

    @Test
    void deleteJournal_works() {
        when(journalRepo.existsByJournalIdAndUser_UserId(456L, 42L)).thenReturn(true);

        boolean result = service.deleteJournal(456L, 42L);

        assertTrue(result);
        verify(journalRepo).deleteById(456L);
    }

    @Test
    void deleteJournal_doesntExist_returnsFalse() {
        when(journalRepo.existsByJournalIdAndUser_UserId(999L, 42L)).thenReturn(false);

        boolean result = service.deleteJournal(999L, 42L);

        assertFalse(result);
        verify(journalRepo, never()).deleteById(anyLong());
    }

    @Test
    void getUserWins_getsWins() {
        List<Win> wins = Arrays.asList(w1);
        when(userRepo.existsById(42L)).thenReturn(true);
        when(winRepo.findByUser_UserId(42L)).thenReturn(wins);

        List<Win> result = service.getUserWins(42L);

        assertEquals(1, result.size());
        verify(winRepo).findByUser_UserId(42L);
    }

    @Test
    void getUserWins_userDoesntExist() {
        when(userRepo.existsById(999L)).thenReturn(false);

        var ex = assertThrows(IllegalArgumentException.class, 
            () -> service.getUserWins(999L));
        
        assertTrue(ex.getMessage().toLowerCase().contains("user") || 
                  ex.getMessage().toLowerCase().contains("not found"));
    }
}