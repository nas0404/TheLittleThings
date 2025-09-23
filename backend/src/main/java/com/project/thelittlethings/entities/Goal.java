package com.project.thelittlethings.entities;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "goals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Goal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="goal_id")
    private Long goalId;

    @ManyToOne(optional = false)
    @JoinColumn(name="user_id", foreignKey=@ForeignKey(name="fk_goals_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name="category_id", foreignKey=@ForeignKey(name="fk_goals_category"))
    private Category category; // nullable

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, length = 10)
    private String priority;

    @Column(name="created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name="updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
} 
