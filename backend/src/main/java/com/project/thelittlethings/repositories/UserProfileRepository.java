package com.project.thelittlethings.repositories;

import com.project.thelittlethings.entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {}
