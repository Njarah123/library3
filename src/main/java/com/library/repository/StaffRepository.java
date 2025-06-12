package com.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.library.model.Staff;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
     @Query("SELECT COUNT(s) > 0 FROM Staff s WHERE s.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
}