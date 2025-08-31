package com.project.thelittlethings.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wins")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Win {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="win_id")
  private Long winId;

  @ManyToOne(optional = false)
  @JoinColumn(name="user_id", foreignKey=@ForeignKey(name="fk_wins_user"))
  private User user;

  @ManyToOne(optional = false)
  @JoinColumn(name="goal_id", foreignKey=@ForeignKey(name="fk_wins_goal"))
  private Goal goal;

  // optional journal reference if you still want it
  @Column(name="journal_id")
  private Long journalId;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(columnDefinition = "text")
  private String description;

  @Column(name="num_trophies")
  private Integer numTrophies = 0;

  @Column(name="completion_date")
  private OffsetDateTime completionDate;
}
