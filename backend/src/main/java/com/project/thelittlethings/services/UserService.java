package com.project.thelittlethings.services;
import java.time.LocalDate;

import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.dto.users.CreateUserRequest;
import com.project.thelittlethings.dto.users.LoginRequest;
import com.project.thelittlethings.repositories.UserRepository;
import com.project.thelittlethings.security.HMACtokens;

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
		if (userRepository.existsByUsername(req.getUsername()) || userRepository.existsByEmail(req.getEmail())) throw new IllegalArgumentException("User already exists");

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

	private String normalizeGender(String raw) {
		if (raw == null || raw.trim().isEmpty()) return "Other";
		String s = raw.trim().toLowerCase();
		if (s.equals("male") || s.equals("m")) return "Male";
		if (s.equals("female") || s.equals("f")) return "Female";
		return "Other";
	}

	private int calcAge(LocalDate dob) {
		if (dob == null) return 0;
		return LocalDate.now().getYear() - dob.getYear();
	}

	public String login(LoginRequest req) {
		Optional<User> userOpt = req.getUsernameOrEmail().contains("@") 
			? userRepository.findByEmail(req.getUsernameOrEmail())
			: userRepository.findByUsername(req.getUsernameOrEmail());
		
		if (userOpt.isEmpty()) throw new IllegalArgumentException("User not found");
		
		User user = userOpt.get();
		if (!user.getPassword().equals(hash(req.getPassword()))) {
			throw new IllegalArgumentException("Wrong password");
		}

		user.setLastLogin(java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC));
		userRepository.save(user);

		return HMACtokens.issueToken(user.getUsername(), 60 * 60 * 24);
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
		u.setPassword(hash(newPassword));
		userRepository.save(u);
	}

	public void resetPassword(String email, String newPassword) {
		User u = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
		u.setPassword(hash(newPassword));
		userRepository.save(u);
	}

	public String changeUsername(Long userId, String newUsername) {
		if (userRepository.existsByUsername(newUsername)) throw new IllegalArgumentException("Username already taken");
		User u = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
		u.setUsername(newUsername);
		userRepository.save(u);
		return HMACtokens.issueToken(u.getUsername(), 60 * 60 * 24);
	}

	public boolean deleteUser(Long userId) {
		if (!userRepository.existsById(userId)) return false;
		userRepository.deleteById(userId);
		return true;
	}
}

