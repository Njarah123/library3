package com.library.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
import java.util.List;
import org.slf4j.Logger;


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
private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    public boolean isUsernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
    }

     public List<User> getAllMembers() {
        // Utiliser UserType.STUDENT pour récupérer les membres
        return userRepository.findByUserTypeOrderByCreatedAtDesc(UserType.STUDENT);
    }

 public User createMember(User user) {
        // Vérifier si l'username existe déjà
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Ce nom d'utilisateur existe déjà");
        }
        
        // Définir la date de création
        user.setCreatedAt(LocalDateTime.now());
        
        // Par défaut, le membre est actif
        user.setActive(true);
        
        // Encoder le mot de passe si nécessaire
        // user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        return userRepository.save(user);
    }


      public User updateUser(User user) {
        logger.info("Mise à jour de l'utilisateur : " + user.getId());
        
        if (user.getId() == null) {
            throw new RuntimeException("ID de l'utilisateur non fourni pour la mise à jour");
        }
        
        User existingUser = getUserById(user.getId());
        
        // Conserver les champs qui ne doivent pas être modifiés
        user.setUsername(existingUser.getUsername());
        user.setPassword(existingUser.getPassword());
        user.setUserType(existingUser.getUserType());
        user.setCreatedAt(existingUser.getCreatedAt());
        
        try {
            return userRepository.save(user);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour de l'utilisateur", e);
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur : " + e.getMessage());
        }
    }

 public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé"));
        
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setActive(userDetails.isActive());
        
        return userRepository.save(user);
    }

    // Méthode pour supprimer un membre
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé"));
        userRepository.delete(user);
    }
@Transactional
    public User updateUserFields(Long id, String name, String email, boolean active) {
        logger.info("Mise à jour des champs de l'utilisateur : " + id);
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + id));
        
        user.setName(name);
        user.setEmail(email);
        user.setActive(active);
        
        return userRepository.save(user);
    }
 public Long getTotalMembers() {
        return userRepository.count();
    }

    public Long getActiveMembersCount() {
        return userRepository.countByActiveTrue();
    }

    public Long getInactiveMembersCount() {
        return userRepository.countByActiveFalse();
    }

    public Long getNewMembersCount() {
        // Calculer la date d'il y a 30 jours
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return userRepository.countByCreatedAtAfter(thirtyDaysAgo);
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
        
        // Ajout de la photo de profil
        if (dto.getProfileImagePath() != null && !dto.getProfileImagePath().isEmpty()) {
            user.setProfileImagePath(dto.getProfileImagePath());
        } else {
            // Définir une image par défaut selon le type d'utilisateur
            String defaultImage = switch (userType) {
                case STUDENT -> "/images/default-student.png";
                case STAFF -> "/images/default-staff.png";
                case LIBRARIAN -> "/images/default-librarian.png";
                default -> "/images/default-profile.png";
            };
            user.setProfileImagePath(defaultImage);
        }

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

    // Méthode pour mettre à jour la photo de profil
    @Transactional
    public void updateProfileImage(String username, String newImagePath) {
        User user = findByUsername(username);
        user.setProfileImagePath(newImagePath);
        userRepository.save(user);
    }
}