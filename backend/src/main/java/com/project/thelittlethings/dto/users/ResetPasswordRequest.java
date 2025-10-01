package com.project.thelittlethings.dto.users;

// for password reset functionality
public class ResetPasswordRequest {
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
