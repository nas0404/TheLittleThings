package com.project.thelittlethings.Service;

import com.project.thelittlethings.dto.journals.CreateJournalRequest;
import com.project.thelittlethings.dto.journals.UpdateJournalRequest;
import com.project.thelittlethings.dto.journals.JournalResponse;
import com.project.thelittlethings.dto.users.CreateUserRequest;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.entities.Journal;
import com.project.thelittlethings.repositories.JournalRepository;
import com.project.thelittlethings.repositories.UserRepository;
import com.project.thelittlethings.services.JournalService;
import com.project.thelittlethings.services.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class JournalServiceTest {

    @Autowired
    private JournalService journalService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JournalRepository journalRepository;
    
    @Autowired
    private UserRepository userRepository;

    private Long testUserId;

    @BeforeEach
    void setUp() {
        journalRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test user
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setUsername("journaluser");
        userRequest.setEmail("journal@example.com");
        userRequest.setPassword("password123");
        userRequest.setFirstName("Journal");
        userRequest.setLastName("User");
        userRequest.setDob(LocalDate.of(1990, 1, 1));
        userRequest.setGender("Male");
        userRequest.setRegion("TestRegion");
        
        User user = userService.register(userRequest);
        testUserId = user.getUserId();
    }

    @Test
    void testCreateJournal_Success() {
        // Arrange
        CreateJournalRequest request = new CreateJournalRequest();
        request.setTitle("Test Entry");
        request.setContent("This is test content for the journal entry.");
        request.setReminderType(Journal.ReminderType.NONE);

        // Act
        JournalResponse response = journalService.createJournal(testUserId, request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getJournalId());
        assertEquals("Test Entry", response.getTitle());
        assertEquals("This is test content for the journal entry.", response.getContent());
        assertEquals(Journal.ReminderType.NONE, response.getReminderType());
    }

    @Test
    void testGetAllJournals_Success() {
        // Arrange - Create multiple journal entries
        CreateJournalRequest request1 = new CreateJournalRequest();
        request1.setTitle("First Entry");
        request1.setContent("First content");
        request1.setReminderType(Journal.ReminderType.DAILY);
        
        CreateJournalRequest request2 = new CreateJournalRequest();
        request2.setTitle("Second Entry");
        request2.setContent("Second content");
        request2.setReminderType(Journal.ReminderType.WEEKLY);

        journalService.createJournal(testUserId, request1);
        journalService.createJournal(testUserId, request2);

        // Act
        List<JournalResponse> journals = journalService.getAllJournals(testUserId, "date");

        // Assert
        assertNotNull(journals);
        assertEquals(2, journals.size());
        
        // Should contain both entries
        assertTrue(journals.stream().anyMatch(j -> "First Entry".equals(j.getTitle())));
        assertTrue(journals.stream().anyMatch(j -> "Second Entry".equals(j.getTitle())));
    }

    @Test
    void testGetAllJournals_SortByTitle() {
        // Arrange - Create entries that will have different alphabetical vs date order
        CreateJournalRequest request1 = new CreateJournalRequest();
        request1.setTitle("Beta Entry");
        request1.setContent("Beta content");
        request1.setReminderType(Journal.ReminderType.NONE);
        
        CreateJournalRequest request2 = new CreateJournalRequest();
        request2.setTitle("Alpha Entry");
        request2.setContent("Alpha content");
        request2.setReminderType(Journal.ReminderType.NONE);

        journalService.createJournal(testUserId, request1);
        journalService.createJournal(testUserId, request2);

        // Act
        List<JournalResponse> journals = journalService.getAllJournals(testUserId, "title");

        // Assert
        assertNotNull(journals);
        assertEquals(2, journals.size());
        
        // When sorted alphabetically, "Alpha Entry" should come first
        assertEquals("Alpha Entry", journals.get(0).getTitle());
        assertEquals("Beta Entry", journals.get(1).getTitle());
    }

    @Test
    void testGetJournal_Success() {
        // Arrange
        CreateJournalRequest request = new CreateJournalRequest();
        request.setTitle("Get Test Entry");
        request.setContent("Content for get test");
        request.setReminderType(Journal.ReminderType.ON_WIN_CREATED);
        
        JournalResponse created = journalService.createJournal(testUserId, request);

        // Act
        JournalResponse retrieved = journalService.getJournal(created.getJournalId(), testUserId);

        // Assert
        assertNotNull(retrieved);
        assertEquals(created.getJournalId(), retrieved.getJournalId());
        assertEquals("Get Test Entry", retrieved.getTitle());
        assertEquals("Content for get test", retrieved.getContent());
        assertEquals(Journal.ReminderType.ON_WIN_CREATED, retrieved.getReminderType());
    }

    @Test
    void testGetJournal_NotFound() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            journalService.getJournal(999L, testUserId);
        });
    }

    @Test
    void testUpdateJournal_Success() {
        // Arrange
        CreateJournalRequest createRequest = new CreateJournalRequest();
        createRequest.setTitle("Original Title");
        createRequest.setContent("Original content");
        createRequest.setReminderType(Journal.ReminderType.NONE);
        
        JournalResponse created = journalService.createJournal(testUserId, createRequest);

        UpdateJournalRequest updateRequest = new UpdateJournalRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setContent("Updated content");
        updateRequest.setReminderType(Journal.ReminderType.DAILY);

        // Act
        JournalResponse updated = journalService.updateJournal(created.getJournalId(), testUserId, updateRequest);

        // Assert
        assertNotNull(updated);
        assertEquals(created.getJournalId(), updated.getJournalId());
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated content", updated.getContent());
        assertEquals(Journal.ReminderType.DAILY, updated.getReminderType());
    }

    @Test
    void testDeleteJournal_Success() {
        // Arrange
        CreateJournalRequest request = new CreateJournalRequest();
        request.setTitle("Delete Test Entry");
        request.setContent("Content to be deleted");
        request.setReminderType(Journal.ReminderType.NONE);
        
        JournalResponse created = journalService.createJournal(testUserId, request);

        // Act
        boolean deleted = journalService.deleteJournal(created.getJournalId(), testUserId);

        // Assert
        assertTrue(deleted);
        
        // Should throw exception when trying to get deleted journal
        assertThrows(IllegalArgumentException.class, () -> {
            journalService.getJournal(created.getJournalId(), testUserId);
        });
    }

    @Test
    void testDeleteJournal_NotFound() {
        // Act
        boolean deleted = journalService.deleteJournal(999L, testUserId);

        // Assert
        assertFalse(deleted);
    }
}