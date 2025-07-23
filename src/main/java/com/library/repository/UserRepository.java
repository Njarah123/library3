package com.library.repository;

import com.library.enums.UserType;
import com.library.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;import java.util.Optional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByUserTypeOrderByCreatedAtDesc(UserType userType);
    List<User> findAllByUserType(UserType userType);
    List<User> findAllByOrderByCreatedAtDesc();
     Long countByActiveTrue();
    Long countByActiveFalse();
    Long countByCreatedAtAfter(LocalDateTime date);
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
long countByCreatedDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
@Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType")
long countByUserType(@Param("userType") String userType);
// Compter les étudiants
@Query("SELECT COUNT(s) FROM Student s")
long countStudents();

// Compter le staff
@Query("SELECT COUNT(s) FROM Staff s")
long countStaff();

// Compter les bibliothécaires
@Query("SELECT COUNT(l) FROM Librarian l")
long countLibrarians();

// Statistiques des types d'utilisateurs
@Query("SELECT u.userType, COUNT(u) FROM User u GROUP BY u.userType")
List<Object[]> getUserTypeStatistics();
}