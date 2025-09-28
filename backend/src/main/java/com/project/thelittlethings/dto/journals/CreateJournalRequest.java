package com.project.thelittlethings.dto.journals;

import com.project.thelittlethings.entities.Journal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateJournalRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be 255 characters or less")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    // Optional linked win ID
    private Long linkedWinId;

    // Reminder type - defaults to NONE if not specified
    private Journal.ReminderType reminderType = Journal.ReminderType.NONE;

    // Constructors
    public CreateJournalRequest() {}

    public CreateJournalRequest(String title, String content, Long linkedWinId, Journal.ReminderType reminderType) {
        this.title = title;
        this.content = content;
        this.linkedWinId = linkedWinId;
        this.reminderType = reminderType;
    }

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getLinkedWinId() { return linkedWinId; }
    public void setLinkedWinId(Long linkedWinId) { this.linkedWinId = linkedWinId; }

    public Journal.ReminderType getReminderType() { return reminderType; }
    public void setReminderType(Journal.ReminderType reminderType) { this.reminderType = reminderType; }
}
