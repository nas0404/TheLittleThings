package com.project.thelittlethings.dto.users;

// login data - can use username or email
public class LoginRequest {
    private String usernameOrEmail;
    private String password;

    // basic getters and setters
    public String getUsernameOrEmail() { return usernameOrEmail; }
    public void setUsernameOrEmail(String usernameOrEmail) { this.usernameOrEmail = usernameOrEmail; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
