package com.project.thelittlethings.entities;
import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

// Category.java
@Entity
@Table(name = "categories",
  uniqueConstraints = @UniqueConstraint(name="uq_categories_user_name", columnNames={"user_id","name"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Category {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="category_id")
  private Long categoryId;


  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name="user_id", foreignKey=@ForeignKey(name="fk_categories_user"))
  @NotNull(message = "user is required")
  private User user;

  @Column(nullable = false, length = 100)
  @NotBlank(message = "name is required")
  @Size(max = 100, message = "name must be ≤ 100 characters")
  private String name;

  @Column(columnDefinition = "text")
  @Size(max = 100, message = "description must be ≤ 100 characters")
  private String description;

  
  @org.hibernate.annotations.CreationTimestamp
  @Column(name="created_at", updatable = false)   // remove insertable=false
  private Instant createdAt;

  @org.hibernate.annotations.UpdateTimestamp
  @Column(name="updated_at")                       // remove insertable/updatable flags
  private Instant updatedAt;
}


