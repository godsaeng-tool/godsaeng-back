package com.example.godsaengbackend.repository;

import com.example.godsaengbackend.entity.StudyRecord;
import com.example.godsaengbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyRecordRepository extends JpaRepository<StudyRecord, Long> {
    Page<StudyRecord> findByUser(User user, Pageable pageable);
    Page<StudyRecord> findByUserAndLectureId(User user, Long lectureId, Pageable pageable);
    Optional<StudyRecord> findByIdAndUser(Long id, User user);
}
