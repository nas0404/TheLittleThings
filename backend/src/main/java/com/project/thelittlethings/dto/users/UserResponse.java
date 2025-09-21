package com.project.thelittlethings.dto.users;

import com.project.thelittlethings.entities.User;

import java.time.LocalDate;

public class UserResponse {
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate dob;
    private String gender;
    private String region;
    private String lastLogin; // ISO-8601 string or null

    public static UserResponse fromUser(User u) {
        UserResponse r = new UserResponse();
        r.userId = u.getUserId();
        r.username = u.getUsername();
        r.email = u.getEmail();
        r.firstName = u.getFirstName();
        r.lastName = u.getLastName();
        r.dob = u.getDob();
        r.gender = u.getGender();
        r.region = u.getRegion();
        // Read lastLogin OffsetDateTime directly from the entity (new dedicated field)
        java.time.OffsetDateTime ll = u.getLastLogin();
        if (ll != null) {
            r.lastLogin = ll.toString();
        } else {
            r.lastLogin = null;
        }
        return r;
    }

    // getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
}
