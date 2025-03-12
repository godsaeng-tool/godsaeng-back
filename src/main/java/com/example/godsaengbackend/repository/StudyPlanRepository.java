package com.example.godsaengbackend.repository;

import com.example.godsaengbackend.entity.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
    // 필요한 쿼리 메서드 추가
}
