package com.project.thelittlethings.entities;
import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
  name = "categories",
  uniqueConstraints = @UniqueConstraint(name="uq_categories_user_name", columnNames={"user_id","name"})
)
// @EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Category {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="category_id")
  private Long categoryId;

  @ManyToOne(optional = false)
  @JoinColumn(name="user_id", foreignKey=@ForeignKey(name="fk_categories_user"))
  private User user;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(columnDefinition = "text")
  private String description;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}


