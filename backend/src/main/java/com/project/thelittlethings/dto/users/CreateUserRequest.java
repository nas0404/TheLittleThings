package com.project.thelittlethings.dto.users;

import java.time.LocalDate;

// request data for user registration
public class CreateUserRequest {
	private String username;
	private String email;
	private String password;
	private String firstName;
	private String lastName;
	private LocalDate dob;
	private String gender;
	private String region;

	// all the getters and setters for form data
	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

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
}
