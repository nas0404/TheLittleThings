package com.project.thelittlethings.repositories;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.thelittlethings.entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  // lookups to be used for user
  Optional<User> findByUsername(String username);
  Optional<User> findByEmail(String email);
  Optional<User> findByUserId(long userId);

  // fast pre-checks for registration updates
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
}