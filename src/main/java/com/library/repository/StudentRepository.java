package com.library.repository;

import com.library.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByStudentId(String studentId);
    java.util.Optional<Student> findByStudentId(String studentId);
}