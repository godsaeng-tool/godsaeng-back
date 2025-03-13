package com.example.godsaengbackend.repository;

import com.example.godsaengbackend.entity.StudyPlan;
import com.example.godsaengbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
    Page<StudyPlan> findByUser(User user, Pageable pageable);
    Page<StudyPlan> findByUserAndLectureId(User user, Long lectureId, Pageable pageable);
    Optional<StudyPlan> findByIdAndUser(Long id, User user);
}
