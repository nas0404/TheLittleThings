package com.project.thelittlethings.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.thelittlethings.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);
  Optional<User> findByEmail(String email);
  Optional<User> findByUserId(long userId);
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);

  List<User> findByRegionOrderByTrophiesDesc(String region, Pageable pageable);

  List<User> findAllByOrderByTrophiesDesc(Pageable pageable);
}