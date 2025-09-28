package com.project.thelittlethings.services;
import java.time.LocalDate;

import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.dto.users.CreateUserRequest;
import com.project.thelittlethings.dto.users.LoginRequest;
import com.project.thelittlethings.repositories.UserRepository;
import com.project.thelittlethings.security.TokenUtil;

import java.time.LocalDate;
import java.util.*;
import java.security.MessageDigest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final Set<String> blacklistedTokens = Collections.synchronizedSet(new HashSet<>());

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

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

	@Transactional
	public synchronized User register(CreateUserRequest req) {
		// basic validation
		if (req.getUsername() == null || req.getUsername().length() < 3) throw new IllegalArgumentException("Invalid username");
		if (req.getEmail() == null || !req.getEmail().contains("@")) throw new IllegalArgumentException("Invalid email");
		if (req.getPassword() == null || req.getPassword().length() < 8) throw new IllegalArgumentException("Invalid password");
	if (userRepository.existsByUsername(req.getUsername()) || userRepository.existsByEmail(req.getEmail())) throw new IllegalArgumentException("User exists");

		User u = new User();
		u.setUsername(req.getUsername());
		u.setEmail(req.getEmail());
		u.setPassword(hash(req.getPassword()));
		u.setFirstName(req.getFirstName());
		u.setLastName(req.getLastName());
		u.setDob(req.getDob());
		// normalize gender to canonical DB values
		u.setGender(normalizeGender(req.getGender()));
		u.setRegion(req.getRegion());
		u.setStreaks(0);
		u.setAge(calcAge(req.getDob()));
		u.setLastLogin(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC));

		return userRepository.save(u);
	}

	/**
	 * Normalize a free-text gender input into one of the canonical values
	 * expected by the database: "Male", "Female", or "Other".
	 *
	 * This accepts common variations (case-insensitive) such as "male", "M", "man",
	 * "female", "F", "woman", and maps anything else (including null/empty)
	 * to "Other".
	 */
	private String normalizeGender(String raw) {
		if (raw == null) return "Other";
		String s = raw.trim().toLowerCase(Locale.ROOT);
		if (s.isEmpty()) return "Other";
		if (s.equals("male") || s.equals("m") || s.equals("man") || s.equals("male ")) return "Male";
		if (s.equals("female") || s.equals("f") || s.equals("woman") || s.equals("female ")) return "Female";
		// common synonyms for non-binary / other
		if (s.equals("non-binary") || s.equals("nonbinary") || s.equals("nb") || s.equals("nonbinary") || s.equals("non-binary") ) return "Other";
		// default fallback
		return "Other";
	}

	private int calcAge(LocalDate dob) {
		if (dob == null) return 0;
		return LocalDate.now().getYear() - dob.getYear();
	}

	public String login(LoginRequest req) {
		Optional<User> opt;
		if (req.getUsernameOrEmail().contains("@")) opt = userRepository.findByEmail(req.getUsernameOrEmail());
		else opt = userRepository.findByUsername(req.getUsernameOrEmail());
		if (opt.isEmpty()) throw new IllegalArgumentException("User not found");
		User u = opt.get();
		if (!u.getPassword().equals(hash(req.getPassword()))) throw new IllegalArgumentException("Invalid credentials");

		// update last login and persist
		u.setLastLogin(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC));
		userRepository.save(u);

		return TokenUtil.issueToken(u.getUsername(), 60 * 60 * 24); // 1 day
	}

	public User findByUsername(String username) {
		return userRepository.findByUsername(username).orElse(null);
	}

	public User findById(Long id) { return userRepository.findById(id).orElse(null); }

	public void logout(String token) { blacklistedTokens.add(token); }

	public boolean isTokenBlacklisted(String token) { return blacklistedTokens.contains(token); }

	public void changePassword(Long userId, String oldPassword, String newPassword) {
		User u = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
		if (!u.getPassword().equals(hash(oldPassword))) throw new IllegalArgumentException("Invalid current password");
		if (newPassword.length() < 8) throw new IllegalArgumentException("New password too short");
		u.setPassword(hash(newPassword));
		userRepository.save(u);
	}

	public void resetPassword(String email, String newPassword) {
		User u = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
		u.setPassword(hash(newPassword));
		userRepository.save(u);
	}

	/**
	 * Change a user's username. Returns a fresh token for the new username.
	 */
	public String changeUsername(Long userId, String newUsername) {
		if (newUsername == null || newUsername.length() < 3) throw new IllegalArgumentException("Invalid username");
		if (userRepository.existsByUsername(newUsername)) throw new IllegalArgumentException("Username already taken");
		User u = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
		u.setUsername(newUsername);
		userRepository.save(u);
		return TokenUtil.issueToken(u.getUsername(), 60 * 60 * 24);
	}

	/**
	 * Delete a user account from the in-memory store. Returns true if deleted.
	 */
	public boolean deleteUser(Long userId) {
		if (!userRepository.existsById(userId)) return false;
		userRepository.deleteById(userId);
		return true;
	}
}

