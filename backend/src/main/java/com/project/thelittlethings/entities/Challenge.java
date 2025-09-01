package com.project.thelittlethings.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "challenges")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Challenge {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="challenge_id")
  private Long challengeId;

  @ManyToOne(optional = false)
  @JoinColumn(name="user_id", foreignKey=@ForeignKey(name="fk_challenges_user"))
  private User user;

  // Prototype: store list of goals as TEXT (CSV or JSON string)
  @Column(name="goal_list", nullable = false, columnDefinition = "text")
  private String goalList;

  @Column(name="target_date")
  private LocalDate targetDate;

  private Integer trophies = 0;

  @Column(name="created_at", insertable=false, updatable=false)
  private java.time.OffsetDateTime createdAt;
}

