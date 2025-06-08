package com.library.repository;

import com.library.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    boolean existsByEmployeeId(String employeeId);
}