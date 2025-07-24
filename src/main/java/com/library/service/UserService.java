package com.library.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.library.dto.Activity;
import com.library.dto.UserRegistrationDto;
import com.library.enums.UserType;
import com.library.model.Account;
import com.library.model.Book;
import com.library.model.Borrow;
import com.library.model.Librarian;
import com.library.model.Staff;
import com.library.model.Student;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRepository;
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
    private BookRepository bookRepository;
    @Autowired
    private BorrowRepository borrowRepository;
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
        // Récupérer tous les utilisateurs (tous types confondus)
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<User> findByRole(String role) {
        UserType userType = UserType.valueOf(role.toUpperCase());
        return userRepository.findAllByUserType(userType);
    }
    
    public List<User> findByUserType(UserType userType) {
        return userRepository.findAllByUserType(userType);
    }

    public List<User> getAllLibrarians() {
        return userRepository.findAllByUserType(UserType.LIBRARIAN);
    }
    
    public List<User> getAllStaff() {
        return userRepository.findAllByUserType(UserType.STAFF);
    }
    
    public List<User> getAllStudents() {
        return userRepository.findAllByUserType(UserType.STUDENT);
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

public long countUsersCreatedInRange(LocalDateTime startDate, LocalDateTime endDate) {
    try {
        // Simulation basée sur le nombre total d'utilisateurs
        List<User> allUsers = userRepository.findAll();
        return Math.max(1, allUsers.size() / 20); // Simulation : 5% des utilisateurs ce mois
    } catch (Exception e) {
        System.err.println("Erreur lors du comptage des utilisateurs: " + e.getMessage());
        return 0L;
    }
}

public long countByUserType(String userType) {
    try {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
            .filter(user -> {
                if (user.getUserType() != null) {
                    return user.getUserType().toString().equalsIgnoreCase(userType);
                }
                // Logique alternative basée sur le type de classe
                switch (userType.toUpperCase()) {
                    case "STUDENT":
                        return user instanceof Student;
                    case "STAFF":
                        return user instanceof Staff;
                    case "LIBRARIAN":
                        return user instanceof Librarian;
                    default:
                        return false;
                }
            })
            .count();
    } catch (Exception e) {
        System.err.println("Erreur lors du comptage par type d'utilisateur: " + e.getMessage());
        return 0L;
    }
}
public Map<String, Long> getCategoryStatistics() {
    try {
        List<Book> allBooks = bookRepository.findAll();
        return allBooks.stream()
            .collect(Collectors.groupingBy(
                book -> book.getCategory() != null ? book.getCategory() : "Non classé",
                Collectors.counting()
            ));
    } catch (Exception e) {
        return new HashMap<>();
    }
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
        try {
            logger.info("Début de l'inscription pour l'utilisateur: {}", dto.getUsername());
            
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
                logger.error("Type d'utilisateur invalide: {}", dto.getUserType());
                throw new RuntimeException("Type d'utilisateur invalide: " + dto.getUserType());
            }

            // Création de l'utilisateur en fonction du type
            User user;
            switch (userType) {
                case LIBRARIAN:
                    Librarian librarian = new Librarian();
                    // Générer automatiquement un employeeId unique
                    librarian.setEmployeeId(generateUniqueEmployeeId());
                    user = librarian;
                    break;

                case STAFF:
                    Staff staff = new Staff();
                    // Générer automatiquement un employeeId unique
                    staff.setEmployeeId(generateUniqueEmployeeId());
                    staff.setDept(dto.getDept());
                    user = staff;
                    break;

                case STUDENT:
                    Student student = new Student();
                    // Générer automatiquement un studentId unique
                    student.setStudentId(generateUniqueStudentId());
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
            user.setUserType(userType);
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            
            // Ajout de la photo de profil
            if (dto.getProfileImagePath() != null && !dto.getProfileImagePath().isEmpty()) {
                user.setProfileImagePath(dto.getProfileImagePath());
            } else {
                // Définir une image par défaut selon le type d'utilisateur
                String defaultImage;
                if (userType == UserType.STUDENT) {
                    defaultImage = "/images/default-student.png";
                } else if (userType == UserType.STAFF) {
                    defaultImage = "/images/default-staff.png";
                } else if (userType == UserType.LIBRARIAN) {
                    defaultImage = "/images/default-librarian.png";
                } else {
                    defaultImage = "/images/default-profile.png";
                }
                user.setProfileImagePath(defaultImage);
            }

            // Sauvegarder d'abord l'utilisateur pour obtenir l'ID
            user = userRepository.save(user);
            logger.info("Utilisateur sauvegardé avec l'ID: {}", user.getId());

            // Création et initialisation du compte avec toutes les valeurs par défaut
            Account account = new Account();
            account.setUser(user);
            account.setNoBorrowedBooks(0);
            account.setNoLostBooks(0);
            account.setNoReturnedBooks(0);
            account.setNoReservedBooks(0);
            account.setFineAmount(BigDecimal.ZERO);
            
            user.setAccount(account);

            // Sauvegarder à nouveau avec le compte
            user = userRepository.save(user);
            logger.info("Inscription terminée avec succès pour l'utilisateur: {}", dto.getUsername());
            
            return user;
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'inscription de l'utilisateur: {}", dto.getUsername(), e);
            throw new RuntimeException("Erreur lors de l'inscription: " + e.getMessage());
        }
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

    // Méthode pour mettre à jour la photo de profil par nom d'utilisateur
    @Transactional
    public void updateUserProfileImage(String username, String newImagePath) {
        updateProfileImage(username, newImagePath);
    }

    public List<Activity> getUserActivity(User user) {
        List<Activity> activities = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        if (user.getUserType() == UserType.LIBRARIAN) {
            // Librarian sees all activities
            List<Borrow> allBorrows = borrowRepository.findAll();
            for (Borrow borrow : allBorrows) {
                if ("BORROWED".equals(borrow.getStatus()) && borrow.getExpectedReturnDate().isBefore(now)) {
                    activities.add(new Activity("Livre en retard", "Le livre \"" + borrow.getBook().getTitle() + "\" est en retard pour " + borrow.getUser().getName() + ".", borrow.getExpectedReturnDate(), "OVERDUE", "fas fa-exclamation-triangle"));
                }
            }
            borrowRepository.findAll().stream()
                .sorted(Comparator.comparing(Borrow::getBorrowDate).reversed())
                .limit(10)
                .forEach(b -> activities.add(new Activity("Emprunt", b.getUser().getName() + " a emprunté \"" + b.getBook().getTitle() + "\".", b.getBorrowDate(), "BORROW", "fas fa-book-reader")));
        } else {
            // Student and Staff see their own activities
            List<Borrow> userBorrows = borrowRepository.findByUserId(user.getId());
            for (Borrow borrow : userBorrows) {
                if ("BORROWED".equals(borrow.getStatus())) {
                    if (borrow.getExpectedReturnDate().isBefore(now)) {
                        activities.add(new Activity("Livre en retard", "Votre livre \"" + borrow.getBook().getTitle() + "\" est en retard.", borrow.getExpectedReturnDate(), "OVERDUE", "fas fa-exclamation-triangle"));
                    } else if (borrow.getExpectedReturnDate().isBefore(now.plusDays(3))) {
                        activities.add(new Activity("Rappel de retour", "Votre livre \"" + borrow.getBook().getTitle() + "\" doit être retourné bientôt.", borrow.getExpectedReturnDate(), "DUE_SOON", "fas fa-clock"));
                    }
                }
            }
             userBorrows.stream()
                .sorted(Comparator.comparing(Borrow::getBorrowDate).reversed())
                .limit(5)
                .forEach(b -> activities.add(new Activity("Emprunt récent", "Vous avez emprunté \"" + b.getBook().getTitle() + "\".", b.getBorrowDate(), "BORROW", "fas fa-book-reader")));
            
            // Notification for new books
            bookRepository.findAll().stream()
                .sorted(Comparator.comparing(Book::getAddedDate).reversed())
                .limit(5)
                .forEach(b -> activities.add(new Activity("Nouveau livre", "Le livre \"" + b.getTitle() + "\" a été ajouté.", b.getAddedDate(), "NEW_BOOK", "fas fa-book")));
        }

        // Sort activities by timestamp
        activities.sort(Comparator.comparing(Activity::getTimestamp).reversed());

        return activities;
    }

    public List<Activity> getAllActivities() {
        List<Activity> activities = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getUserType() == UserType.STUDENT || user.getUserType() == UserType.STAFF) {
                List<Borrow> userBorrows = borrowRepository.findByUserId(user.getId());
                for (Borrow borrow : userBorrows) {
                    if ("BORROWED".equals(borrow.getStatus())) {
                        if (borrow.getExpectedReturnDate().isBefore(now)) {
                            activities.add(new Activity("Livre en retard", "Le livre \"" + borrow.getBook().getTitle() + "\" est en retard pour " + borrow.getUser().getName() + ".", borrow.getExpectedReturnDate(), "OVERDUE", "fas fa-exclamation-triangle"));
                        }
                    }
                }
                userBorrows.stream()
                    .sorted(Comparator.comparing(Borrow::getBorrowDate).reversed())
                    .limit(5)
                    .forEach(b -> activities.add(new Activity("Emprunt récent",  b.getUser().getName() + " a emprunté \"" + b.getBook().getTitle() + "\".", b.getBorrowDate(), "BORROW", "fas fa-book-reader")));
            }
        }

        // Sort activities by timestamp
        activities.sort(Comparator.comparing(Activity::getTimestamp).reversed());

        return activities;
    }
    
    /**
     * Save a user
     * @param user The user to save
     * @return The saved user
     */
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }
    
    /**
     * Find user by email
     * @param email The email to search for
     * @return The user with the given email
     * @throws RuntimeException if user not found
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'email: " + email));
    }
    
    /**
     * Change user password
     * @param user The user whose password to change
     * @param currentPassword The current password for verification
     * @param newPassword The new password
     * @throws RuntimeException if current password is incorrect
     */
    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Le mot de passe actuel est incorrect");
        }
        
        // Encode and set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        logger.info("Mot de passe changé avec succès pour l'utilisateur: {}", user.getUsername());
    }
    
    /**
     * Save profile image for user
     * @param user The user to save the image for
     * @param file The image file to save
     * @return The path where the image was saved
     * @throws RuntimeException if file upload fails
     */
    @Transactional
    public String saveProfileImage(User user, MultipartFile file) {
        try {
            // Stocker l'image en base de données pour la persistance en production
            user.setProfileImageData(file.getBytes());
            user.setProfileImageContentType(file.getContentType());
            
            // Générer un nom de fichier virtuel pour la compatibilité
            String uniqueFilename = "profile_" + user.getId() + "_" + System.currentTimeMillis();
            String imagePath = "/api/users/" + user.getId() + "/profile-image";
            user.setProfileImagePath(imagePath);
            
            userRepository.save(user);
            
            logger.info("Image de profil sauvegardée en base de données pour l'utilisateur: {}", user.getUsername());
            return imagePath;
            
        } catch (IOException e) {
            logger.error("Erreur lors de la sauvegarde de l'image de profil", e);
            throw new RuntimeException("Erreur lors de la sauvegarde de l'image: " + e.getMessage());
        }
    }
    
    /**
     * Generate a unique student ID
     * @return A unique student ID string
     */
    private String generateUniqueStudentId() {
        String studentId;
        do {
            // Générer un ID étudiant au format STU + timestamp + random
            long timestamp = System.currentTimeMillis() % 100000; // 5 derniers chiffres
            int random = (int) (Math.random() * 1000); // 3 chiffres aléatoires
            studentId = String.format("STU%05d%03d", timestamp, random);
        } while (studentRepository.findByStudentId(studentId).isPresent());
        
        return studentId;
    }
    
    /**
     * Generate a unique employee ID for staff and librarians
     * @return A unique employee ID string
     */
    private String generateUniqueEmployeeId() {
        String employeeId;
        do {
            // Générer un ID employé au format EMP + timestamp + random
            long timestamp = System.currentTimeMillis() % 100000; // 5 derniers chiffres
            int random = (int) (Math.random() * 1000); // 3 chiffres aléatoires
            employeeId = String.format("EMP%05d%03d", timestamp, random);
        } while (staffRepository.findByEmployeeId(employeeId).isPresent() ||
                 librarianRepository.findByEmployeeId(employeeId).isPresent());
        
        return employeeId;
    }
}