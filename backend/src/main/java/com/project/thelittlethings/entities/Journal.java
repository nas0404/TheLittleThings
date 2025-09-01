package com.project.thelittlethings.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "journaling")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Journal {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="journal_id")
  private Long journalId;

  @ManyToOne(optional = false)
  @JoinColumn(name="user_id", foreignKey=@ForeignKey(name="fk_j_user"))
  private User user;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(nullable = false, columnDefinition = "text")
  private String content;

  @Column(name="created_at", insertable=false, updatable=false)
  private java.time.OffsetDateTime createdAt;

  @Column(name="updated_at", insertable=false, updatable=false)
  private java.time.OffsetDateTime updatedAt;
}

