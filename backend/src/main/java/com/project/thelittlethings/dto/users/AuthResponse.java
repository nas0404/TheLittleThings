package com.project.thelittlethings.dto.users;

// response after sucessful login/register
public class AuthResponse {
    private String token;
    private Long userId;
    private String username;

    public AuthResponse() {}
    public AuthResponse(String token, Long userId, String username) {
        this.token = token; this.userId = userId; this.username = username;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
