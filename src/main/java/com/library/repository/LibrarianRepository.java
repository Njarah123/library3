package com.library.repository;

import com.library.model.Librarian;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibrarianRepository extends JpaRepository<Librarian, Long> {
    boolean existsByEmployeeId(String employeeId);
}