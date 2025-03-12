package com.example.godsaengbackend.repository;

import com.example.godsaengbackend.entity.StudyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyRecordRepository extends JpaRepository<StudyRecord, Long> {
    // 필요한 쿼리 메서드 추가
}
