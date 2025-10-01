package com.project.thelittlethings.entities;

import java.time.Instant;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "goals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Goal {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="goal_id")
    private Long goalId;

    @ManyToOne(optional = false)
    @JoinColumn(name="user_id", nullable = false,
        foreignKey=@ForeignKey(name="fk_goals_user"))
    @NotNull(message = "user is required")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name="category_id", nullable = false,
        foreignKey=@ForeignKey(name="fk_goals_category"))
    @NotNull(message = "category is required")
    private Category category; 

    @Column(nullable = false, length = 255)
    @NotBlank(message = "title is required")
    @Size(max = 255, message = "title must be ≤ 255 characters")
    private String title;

    @Column(columnDefinition = "text")
    @Size(max = 100, message = "description must be ≤ 100 characters")
    private String description;

    @Column(nullable = false, length = 10)
    @NotBlank(message = "priority is required")
    @Pattern(regexp = "^(?i)(HIGH|MEDIUM|LOW)$",
             message = "priority must be HIGH, MEDIUM, or LOW")
    private String priority;

    @Column(name="created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name="updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}
