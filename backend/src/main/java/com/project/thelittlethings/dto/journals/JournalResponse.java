package com.project.thelittlethings.dto.journals;

import com.project.thelittlethings.entities.Journal;
import java.time.format.DateTimeFormatter;

public class JournalResponse {
    private Long journalId;
    private String title;
    private String content;
    private Long linkedWinId;
    private String linkedWinTitle; // For convenience in UI
    private Journal.ReminderType reminderType;
    private String createdAt;
    private String updatedAt;

    // Constructors
    public JournalResponse() {}

    public JournalResponse(Long journalId, String title, String content, Long linkedWinId, 
                          String linkedWinTitle, Journal.ReminderType reminderType, 
                          String createdAt, String updatedAt) {
        this.journalId = journalId;
        this.title = title;
        this.content = content;
        this.linkedWinId = linkedWinId;
        this.linkedWinTitle = linkedWinTitle;
        this.reminderType = reminderType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Static factory method to create from Journal entity
    public static JournalResponse fromJournal(Journal journal) {
        JournalResponse response = new JournalResponse();
        response.journalId = journal.getJournalId();
        response.title = journal.getTitle();
        response.content = journal.getContent();
        response.linkedWinId = journal.getLinkedWin() != null ? journal.getLinkedWin().getWinId() : null;
        response.linkedWinTitle = journal.getLinkedWin() != null ? journal.getLinkedWin().getTitle() : null;
        response.reminderType = journal.getReminderType();
        
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        response.createdAt = journal.getCreatedAt() != null ? journal.getCreatedAt().format(formatter) : null;
        response.updatedAt = journal.getUpdatedAt() != null ? journal.getUpdatedAt().format(formatter) : null;
        
        return response;
    }

    // Getters and setters
    public Long getJournalId() { return journalId; }
    public void setJournalId(Long journalId) { this.journalId = journalId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getLinkedWinId() { return linkedWinId; }
    public void setLinkedWinId(Long linkedWinId) { this.linkedWinId = linkedWinId; }

    public String getLinkedWinTitle() { return linkedWinTitle; }
    public void setLinkedWinTitle(String linkedWinTitle) { this.linkedWinTitle = linkedWinTitle; }

    public Journal.ReminderType getReminderType() { return reminderType; }
    public void setReminderType(Journal.ReminderType reminderType) { this.reminderType = reminderType; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
