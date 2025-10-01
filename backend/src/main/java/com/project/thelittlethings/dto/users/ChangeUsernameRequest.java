package com.project.thelittlethings.dto.users;

// simple request for username changes
public class ChangeUsernameRequest {
    private String newUsername;

    public String getNewUsername() { return newUsername; }
    public void setNewUsername(String newUsername) { this.newUsername = newUsername; }
}
