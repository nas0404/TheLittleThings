package com.project.thelittlethings.dto.journals;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// this is what gets sent from frontend when creating journals
// validation stuff to check the data is ok
public class CreateJournalRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be 255 characters or less")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private Long linkedWinId; // optional field - links journal to a win if they want

    
    public CreateJournalRequest() {}

    // constructor for when we want to create it programatically
    public CreateJournalRequest(String title, String content, Long linkedWinId) {
        this.title = title;
        this.content = content;
        this.linkedWinId = linkedWinId;
    }

    // getters and setters - boilerplate stuff
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getLinkedWinId() { return linkedWinId; }
    public void setLinkedWinId(Long linkedWinId) { this.linkedWinId = linkedWinId; }
}
