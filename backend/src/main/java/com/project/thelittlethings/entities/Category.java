package com.project.thelittlethings.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.Instant;

// Represents a category created by a user (e.g. Health, Work, Study)
@Entity
@Table(name = "categories", uniqueConstraints = @UniqueConstraint(name = "uq_categories_user_name", columnNames = {
    "user_id", "name" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "category_id")
  private Long categoryId;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_categories_user"))
  @NotNull(message = "user is required")
  private User user;

  @Column(name = "name", nullable = false, length = 100)
  @NotBlank(message = "name is required")
  @Size(max = 100, message = "name must be ≤ 100 characters")
  private String name;

  @Column(columnDefinition = "text")
  @Size(max = 100, message = "description must be ≤ 100 characters")
  private String description;

  @org.hibernate.annotations.CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @org.hibernate.annotations.UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;
}
