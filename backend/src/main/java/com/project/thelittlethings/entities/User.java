package com.project.thelittlethings.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="user_id")
  private Long userId;

  @Column(nullable=false, unique=true, length=50)
  private String username;

  @Column(nullable=false, unique=true, length=100)
  private String email;

  @Column(nullable=false, length=255)
  private String password;

  @Column(name="first_name", nullable=false, length=50)
  private String firstName;

  @Column(name="last_name", nullable=false, length=50)
  private String lastName;

  @Column(nullable=false)
  private LocalDate dob;

  private Integer age;
  @Column(name = "last_login")
  private OffsetDateTime lastLogin;
  private String gender;
  private Integer streaks;
  private String region;

  // Method to calculate age from date of birth
  @Transient
  public int getCalculatedAge() {
    if (dob == null) {
      return 0;
    }
    return Period.between(dob, LocalDate.now()).getYears();
  }

  // Method to update the age field based on dob
  @PrePersist
  @PreUpdate
  public void updateAge() {
    if (dob != null) {
      this.age = getCalculatedAge();
    }
  }
}
