package com.library.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.dto.UserRegistrationDto;
import com.library.enums.UserType;
import com.library.model.Account;
import com.library.model.Librarian;
import com.library.model.Staff;
import com.library.model.Student;
import com.library.model.User;
import com.library.repository.LibrarianRepository;
import com.library.repository.StaffRepository;
import com.library.repository.StudentRepository;
import com.library.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StaffRepository staffRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private LibrarianRepository librarianRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean isUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public User registerNewUser(UserRegistrationDto dto) {
        // Vérification du nom d'utilisateur
        if (isUsernameExists(dto.getUsername())) {
            throw new RuntimeException("Ce nom d'utilisateur existe déjà");
        }

        // Vérification de l'email
        if (isEmailExists(dto.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // Conversion sécurisée du type d'utilisateur
        UserType userType;
        try {
            userType = UserType.fromString(dto.getUserType());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Type d'utilisateur invalide: " + dto.getUserType());
        }

        // Création de l'utilisateur en fonction du type
         User user;
        switch (UserType.valueOf(dto.getUserType().toUpperCase())) {
            case LIBRARIAN:
                Librarian librarian = new Librarian();
                librarian.setEmployeeId(dto.getEmployeeId());
                user = librarian;
                break;

            case STAFF:
                Staff staff = new Staff();
                staff.setEmployeeId(dto.getEmployeeId());
                staff.setDept(dto.getDept());
                user = staff;
                break;

            case STUDENT:
                Student student = new Student();
                student.setStudentId(dto.getStudentId());
                student.setClassName(dto.getClassName());
                user = student;
                break;

            default:
                throw new RuntimeException("Type d'utilisateur non supporté");
        }

        // Configuration des champs communs
        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setUserType(UserType.valueOf(dto.getUserType().toUpperCase()));

        // Création et initialisation du compte avec toutes les valeurs par défaut
        Account account = new Account();
        account.setUser(user);
        account.setNoBorrowedBooks(0);
        account.setNoLostBooks(0);
        account.setNoReturnedBooks(0);
        account.setNoReservedBooks(0);
        account.setFineAmount(BigDecimal.ZERO);
        
        user.setAccount(account);

        return userRepository.save(user);
    }
 public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + username));
    }
}