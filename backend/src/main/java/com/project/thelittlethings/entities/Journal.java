package com.project.thelittlethings.entities;


import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

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

  // Optional link to a Win entry for reflection
  @ManyToOne
  @JoinColumn(name="linked_win_id", foreignKey=@ForeignKey(name="fk_journal_win"))
  private Win linkedWin;

  // Reminder settings: DAILY, WEEKLY, ON_WIN_CREATED, NONE
  @Enumerated(EnumType.STRING)
  @Column(name="reminder_type")
  private ReminderType reminderType = ReminderType.NONE;

  @Column(name="created_at", insertable=false, updatable=false)
  private OffsetDateTime createdAt;

  @Column(name="updated_at", insertable=false, updatable=false)
  private OffsetDateTime updatedAt;

  public enum ReminderType {
    DAILY, WEEKLY, ON_WIN_CREATED, NONE
  }
}

