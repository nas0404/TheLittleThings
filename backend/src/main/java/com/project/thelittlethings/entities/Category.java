package com.project.thelittlethings.entities;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
  name = "categories",
  uniqueConstraints = @UniqueConstraint(name="uq_categories_user_name", columnNames={"user_id","name"})
)
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

  // DB-managed timestamps (from DEFAULT NOW())
  @Column(name="created_at", insertable=false, updatable=false)
  private java.time.OffsetDateTime createdAt;

  @Column(name="updated_at", insertable=false, updatable=false)
  private java.time.OffsetDateTime updatedAt;
}

