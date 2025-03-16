package com.example.godsaengbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_plans", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "lecture_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @Column(name = "plan_details", columnDefinition = "TEXT", nullable = false)
    private String planDetails;

    @Enumerated(EnumType.STRING)
    @Column
    private Status status = Status.PENDING;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED
    }
}
