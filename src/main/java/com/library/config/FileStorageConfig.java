// src/main/java/com/library/config/FileStorageConfig.java

package com.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig {
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        // Skip file storage initialization in production to avoid permission issues
        if (System.getProperty("spring.profiles.active", "").contains("prod")) {
            System.out.println("=== PRODUCTION MODE: SKIPPING FILE STORAGE INITIALIZATION ===");
            // Use system temp directory for uploads in production
            uploadDir = System.getProperty("java.io.tmpdir") + "/uploads";
            System.out.println("Using temporary directory for uploads: " + uploadDir);
            return;
        }
        try {
            System.out.println("=== INITIALISATION DES RÉPERTOIRES DE STOCKAGE ===");
            System.out.println("Répertoire de base: " + uploadDir);
            
            // Créer le répertoire principal de téléchargement
            Path mainDir = Paths.get(uploadDir);
            if (!Files.exists(mainDir)) {
                Files.createDirectories(mainDir);
                System.out.println("Répertoire principal créé: " + mainDir);
            } else {
                System.out.println("Répertoire principal existe déjà: " + mainDir);
            }
            
            // Créer les sous-répertoires nécessaires
            String[] subDirs = {"book-covers", "profiles", "static", "images"};
            for (String subDir : subDirs) {
                Path path = mainDir.resolve(subDir);
                if (!Files.exists(path)) {
                    Files.createDirectories(path);
                    System.out.println("Sous-répertoire créé: " + path);
                } else {
                    System.out.println("Sous-répertoire existe déjà: " + path);
                }
            }
            
            // Vérifier si l'image par défaut existe, sinon la créer
            try {
                initDefaultBookCover();
                
                // Créer également une copie de l'image par défaut dans le répertoire /images/
                copyDefaultImageToImagesDir();
            } catch (IOException e) {
                System.err.println("Erreur lors de l'initialisation de l'image par défaut: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("=== FIN DE L'INITIALISATION DES RÉPERTOIRES DE STOCKAGE ===");
        } catch (IOException e) {
            System.err.println("Erreur lors de la création des répertoires: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Could not create upload directories!", e);
        }
    }
    
    /**
     * Initialise l'image de couverture par défaut si elle n'existe pas
     */
    private void initDefaultBookCover() throws IOException {
        try {
            System.out.println("=== INITIALISATION DE L'IMAGE DE COUVERTURE PAR DÉFAUT ===");
            
            java.nio.file.Path defaultBookCoverPath = Paths.get(uploadDir).resolve("book-covers").resolve("default-book.jpg");
            System.out.println("Chemin de l'image par défaut: " + defaultBookCoverPath);
            
            // Si l'image par défaut n'existe pas ou est vide, la créer
            if (!Files.exists(defaultBookCoverPath) || Files.size(defaultBookCoverPath) == 0) {
                System.out.println("L'image par défaut n'existe pas ou est vide");
                
                // Essayer plusieurs chemins possibles pour l'image par défaut dans les ressources
                java.io.InputStream inputStream = null;
                String[] possiblePaths = {
                    "static/images/default-book-cover.jpg",
                    "static/img/default-book-cover.jpg",
                    "static/images/default-book.jpg",
                    "images/default-book-cover.jpg",
                    "img/default-book-cover.jpg",
                    "static/default-book.jpg",
                    "default-book.jpg"
                };
                
                for (String path : possiblePaths) {
                    System.out.println("Recherche de l'image par défaut dans les ressources: " + path);
                    inputStream = getClass().getClassLoader().getResourceAsStream(path);
                    if (inputStream != null) {
                        System.out.println("Image par défaut trouvée dans les ressources: " + path);
                        break;
                    }
                }
                
                if (inputStream != null) {
                    // S'assurer que le répertoire parent existe
                    Files.createDirectories(defaultBookCoverPath.getParent());
                    
                    // Copier l'image depuis les ressources
                    Files.copy(inputStream, defaultBookCoverPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    inputStream.close();
                    System.out.println("Image par défaut copiée avec succès depuis les ressources");
                    
                    // Vérifier que l'image a bien été copiée
                    if (Files.exists(defaultBookCoverPath) && Files.size(defaultBookCoverPath) > 0) {
                        System.out.println("Vérification réussie: l'image par défaut existe et n'est pas vide");
                    } else {
                        System.out.println("ATTENTION: L'image par défaut n'a pas été correctement copiée");
                        createBasicImage(defaultBookCoverPath);
                    }
                } else {
                    // Si l'image n'existe pas dans les ressources, créer une image basique
                    System.out.println("Aucune image par défaut trouvée dans les ressources, création d'une image basique");
                    createBasicImage(defaultBookCoverPath);
                }
            } else {
                System.out.println("L'image par défaut existe déjà: " + defaultBookCoverPath);
                System.out.println("Taille de l'image: " + Files.size(defaultBookCoverPath) + " octets");
            }
            
            System.out.println("=== FIN DE L'INITIALISATION DE L'IMAGE DE COUVERTURE PAR DÉFAUT ===");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'initialisation de l'image par défaut: " + e.getMessage());
            e.printStackTrace();
            // Ne pas bloquer le démarrage de l'application si l'image par défaut ne peut pas être créée
            try {
                // Dernier recours: créer une image basique directement
                java.nio.file.Path defaultBookCoverPath = Paths.get(uploadDir).resolve("book-covers").resolve("default-book.jpg");
                System.out.println("Tentative de création d'une image basique en dernier recours");
                createBasicImage(defaultBookCoverPath);
            } catch (Exception ex) {
                System.err.println("Échec de la création de l'image basique en dernier recours: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Crée une image basique pour servir de couverture par défaut
     * @param path Chemin où créer l'image
     */
    private void createBasicImage(java.nio.file.Path path) {
        try {
            System.out.println("=== CRÉATION D'UNE IMAGE BASIQUE ===");
            System.out.println("Chemin de l'image à créer: " + path);
            
            // S'assurer que le répertoire parent existe
            Files.createDirectories(path.getParent());
            
            // Créer une image basique de 200x300 pixels (taille typique d'une couverture de livre)
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                200, 300, java.awt.image.BufferedImage.TYPE_INT_RGB);
            
            // Remplir avec une couleur de fond
            java.awt.Graphics2D g = img.createGraphics();
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, 200, 300);
            
            // Ajouter un cadre
            g.setColor(java.awt.Color.GRAY);
            g.drawRect(5, 5, 190, 290);
            
            // Ajouter du texte
            g.setColor(java.awt.Color.BLACK);
            g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
            g.drawString("Livre", 75, 100);
            g.drawString("Sans", 75, 150);
            g.drawString("Image", 75, 200);
            
            g.dispose();
            
            // Enregistrer l'image
            javax.imageio.ImageIO.write(img, "jpg", path.toFile());
            System.out.println("Image basique créée avec succès: " + path);
            
            // Vérifier que l'image a bien été créée
            if (Files.exists(path) && Files.size(path) > 0) {
                System.out.println("Vérification réussie: l'image existe et n'est pas vide");
                System.out.println("Taille de l'image: " + Files.size(path) + " octets");
            } else {
                System.out.println("ATTENTION: L'image n'a pas été correctement créée");
                // Essayer une autre méthode
                createSimpleImage(path);
            }
            
            System.out.println("=== FIN DE LA CRÉATION D'UNE IMAGE BASIQUE ===");
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de l'image basique: " + e.getMessage());
            e.printStackTrace();
            
            // Essayer une méthode plus simple
            try {
                createSimpleImage(path);
            } catch (Exception ex) {
                System.err.println("Erreur lors de la création de l'image simple: " + ex.getMessage());
                
                // En dernier recours, créer un fichier vide
                try {
                    Files.createFile(path);
                    System.out.println("Fichier vide créé comme dernier recours: " + path);
                } catch (IOException ioe) {
                    System.err.println("Impossible de créer même un fichier vide: " + ioe.getMessage());
                }
            }
        }
    }
    
    /**
     * Méthode alternative pour créer une image très simple
     * @param path Chemin où créer l'image
     */
    private void createSimpleImage(java.nio.file.Path path) throws IOException {
        System.out.println("Tentative de création d'une image simple");
        
        // Créer une image très simple (1x1 pixel)
        byte[] imageData = new byte[] {
            (byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE0, 0x00, 0x10, 0x4A, 0x46,
            0x49, 0x46, 0x00, 0x01, 0x01, 0x01, 0x00, 0x48, 0x00, 0x48, 0x00, 0x00,
            (byte)0xFF, (byte)0xDB, 0x00, 0x43, 0x00, 0x08, 0x06, 0x06, 0x07, 0x06,
            0x05, 0x08, 0x07, 0x07, 0x07, 0x09, 0x09, 0x08, 0x0A, 0x0C, 0x14, 0x0D,
            0x0C, 0x0B, 0x0B, 0x0C, 0x19, 0x12, 0x13, 0x0F, 0x14, 0x1D, 0x1A, 0x1F,
            0x1E, 0x1D, 0x1A, 0x1C, 0x1C, 0x20, 0x24, 0x2E, 0x27, 0x20, 0x22, 0x2C,
            0x23, 0x1C, 0x1C, 0x28, 0x37, 0x29, 0x2C, 0x30, 0x31, 0x34, 0x34, 0x34,
            0x1F, 0x27, 0x39, 0x3D, 0x38, 0x32, 0x3C, 0x2E, 0x33, 0x34, 0x32,
            (byte)0xFF, (byte)0xC0, 0x00, 0x0B, 0x08, 0x00, 0x01, 0x00, 0x01, 0x01,
            0x01, 0x11, 0x00, (byte)0xFF, (byte)0xC4, 0x00, 0x14, 0x00, 0x01, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, (byte)0xFF, (byte)0xC4, 0x00, 0x14, 0x10, 0x01, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, (byte)0xFF, (byte)0xDA, 0x00, 0x08, 0x01, 0x01, 0x00,
            0x00, 0x3F, 0x00, (byte)0xD2, (byte)0xCF, 0x20, (byte)0xFF, (byte)0xD9
        };
        
        Files.write(path, imageData);
        System.out.println("Image simple créée avec succès: " + path);
    }
    
    /**
     * Copie l'image par défaut dans le répertoire /images/
     * pour assurer la compatibilité avec les chemins d'image commençant par /images/
     */
    private void copyDefaultImageToImagesDir() throws IOException {
        System.out.println("=== COPIE DE L'IMAGE PAR DÉFAUT DANS LE RÉPERTOIRE IMAGES ===");
        
        // Chemin source (image par défaut dans book-covers)
        Path sourceImagePath = Paths.get(uploadDir).resolve("book-covers").resolve("default-book.jpg");
        
        // Chemin destination (dans le répertoire images)
        Path imagesDir = Paths.get(uploadDir).resolve("images");
        Path targetImagePath = imagesDir.resolve("default-book.jpg");
        
        System.out.println("Copie de " + sourceImagePath + " vers " + targetImagePath);
        
        // Vérifier si l'image source existe
        if (Files.exists(sourceImagePath) && Files.size(sourceImagePath) > 0) {
            // S'assurer que le répertoire de destination existe
            if (!Files.exists(imagesDir)) {
                Files.createDirectories(imagesDir);
                System.out.println("Répertoire images créé: " + imagesDir);
            }
            
            // Copier l'image
            Files.copy(sourceImagePath, targetImagePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Image copiée avec succès dans le répertoire images");
            
            // Copier également sous le nom dark.jpg pour la compatibilité avec les tests
            Path darkImagePath = imagesDir.resolve("dark.jpg");
            Files.copy(sourceImagePath, darkImagePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Image copiée également sous le nom dark.jpg pour la compatibilité");
        } else {
            System.out.println("L'image source n'existe pas ou est vide, impossible de la copier");
        }
        
        System.out.println("=== FIN DE LA COPIE DE L'IMAGE PAR DÉFAUT ===");
    }

    public String getUploadDir() {
        return uploadDir;
    }
}