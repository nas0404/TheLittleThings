package com.project.thelittlethings.services;

import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.dto.users.CreateUserRequest;
import com.project.thelittlethings.dto.users.LoginRequest;
import com.project.thelittlethings.security.TokenUtil;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.security.MessageDigest;

import org.springframework.stereotype.Service;

@Service
public class UserService {
	private final Map<Long, User> users = new ConcurrentHashMap<>();
	private final Map<String, Long> usernameIndex = new ConcurrentHashMap<>();
	private final Map<String, Long> emailIndex = new ConcurrentHashMap<>();
	private final Set<String> blacklistedTokens = Collections.newSetFromMap(new ConcurrentHashMap<>());

	private long nextId = 1L;

	// naive hashing for demo only
	private String hash(String s) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] b = md.digest(s.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder();
			for (byte x : b) sb.append(String.format("%02x", x));
			return sb.toString();
		} catch (Exception e) { throw new RuntimeException(e); }
	}

	public synchronized User register(CreateUserRequest req) {
		// basic validation
		if (req.getUsername() == null || req.getUsername().length() < 3) throw new IllegalArgumentException("Invalid username");
		if (req.getEmail() == null || !req.getEmail().contains("@")) throw new IllegalArgumentException("Invalid email");
		if (req.getPassword() == null || req.getPassword().length() < 8) throw new IllegalArgumentException("Invalid password");
		if (usernameIndex.containsKey(req.getUsername()) || emailIndex.containsKey(req.getEmail())) throw new IllegalArgumentException("User exists");

		User u = new User();
		u.setUserId(nextId);
		u.setUsername(req.getUsername());
		u.setEmail(req.getEmail());
		u.setPassword(hash(req.getPassword()));
		u.setFirstName(req.getFirstName());
		u.setLastName(req.getLastName());
		u.setDob(req.getDob());
		u.setGender(req.getGender());
		u.setRegion(req.getRegion());
		u.setStreaks(0);
		u.setAge(calcAge(req.getDob()));
		// set initial lastLogin to now
		u.setLastLogin(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC));

		users.put(nextId, u);
		usernameIndex.put(u.getUsername(), nextId);
		emailIndex.put(u.getEmail(), nextId);
		nextId++;
		return u;
	}

	private int calcAge(LocalDate dob) {
		if (dob == null) return 0;
		return LocalDate.now().getYear() - dob.getYear();
	}

	public String login(LoginRequest req) {
		Long id = null;
		if (req.getUsernameOrEmail().contains("@")) id = emailIndex.get(req.getUsernameOrEmail());
		else id = usernameIndex.get(req.getUsernameOrEmail());
		if (id == null) throw new IllegalArgumentException("User not found");
		User u = users.get(id);
		if (!u.getPassword().equals(hash(req.getPassword()))) throw new IllegalArgumentException("Invalid credentials");

	// update last login as OffsetDateTime
	u.setLastLogin(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC));

		return TokenUtil.issueToken(u.getUsername(), 60 * 60 * 24); // 1 day
	}

	public User findByUsername(String username) {
		Long id = usernameIndex.get(username);
		if (id == null) return null;
		return users.get(id);
	}

	public User findById(Long id) { return users.get(id); }

	public void logout(String token) { blacklistedTokens.add(token); }

	public boolean isTokenBlacklisted(String token) { return blacklistedTokens.contains(token); }

	public void changePassword(Long userId, String oldPassword, String newPassword) {
		User u = users.get(userId);
		if (u == null) throw new IllegalArgumentException("User not found");
		if (!u.getPassword().equals(hash(oldPassword))) throw new IllegalArgumentException("Invalid current password");
		if (newPassword.length() < 8) throw new IllegalArgumentException("New password too short");
		u.setPassword(hash(newPassword));
	}

	public void resetPassword(String email, String newPassword) {
		Long id = emailIndex.get(email);
		if (id == null) throw new IllegalArgumentException("User not found");
		User u = users.get(id);
		u.setPassword(hash(newPassword));
	}

	/**
	 * Change a user's username. Returns a fresh token for the new username.
	 */
	public String changeUsername(Long userId, String newUsername) {
		if (newUsername == null || newUsername.length() < 3) throw new IllegalArgumentException("Invalid username");
		if (usernameIndex.containsKey(newUsername)) throw new IllegalArgumentException("Username already taken");
		User u = users.get(userId);
		if (u == null) throw new IllegalArgumentException("User not found");
		// update index
		usernameIndex.remove(u.getUsername());
		u.setUsername(newUsername);
		usernameIndex.put(newUsername, userId);
		// issue a new token bound to the new username
		return TokenUtil.issueToken(u.getUsername(), 60 * 60 * 24);
	}
}

