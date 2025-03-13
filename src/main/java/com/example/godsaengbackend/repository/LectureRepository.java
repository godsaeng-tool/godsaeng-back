package com.example.godsaengbackend.repository;

import com.example.godsaengbackend.entity.Lecture;
import com.example.godsaengbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    Page<Lecture> findByUser(User user, Pageable pageable);
    Optional<Lecture> findByIdAndUser(Long id, User user);
}
