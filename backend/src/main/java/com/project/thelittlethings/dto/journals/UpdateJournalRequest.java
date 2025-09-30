package com.project.thelittlethings.dto.journals;

import jakarta.validation.constraints.Size;

public class UpdateJournalRequest {
    @Size(max = 255, message = "Title must be 255 characters or less")
    private String title;

    private String content;

    // Optional linked win ID - can be set to null to unlink
    private Long linkedWinId;

    // Constructors
    public UpdateJournalRequest() {}

    public UpdateJournalRequest(String title, String content, Long linkedWinId) {
        this.title = title;
        this.content = content;
        this.linkedWinId = linkedWinId;
    }

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getLinkedWinId() { return linkedWinId; }
    public void setLinkedWinId(Long linkedWinId) { this.linkedWinId = linkedWinId; }
}
