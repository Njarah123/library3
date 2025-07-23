// src/main/java/com/library/service/FileStorageService.java

package com.library.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.library.config.FileStorageConfig;

@Service
public class FileStorageService {
    
    @Autowired
    private FileStorageConfig fileStorageConfig;

    public String storeFile(MultipartFile file) {
        return storeFile(file, "profiles");
    }
    
    public String storeFile(MultipartFile file, String subDirectory) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }

            // Générer un nom de fichier unique
            String fileName = StringUtils.cleanPath(UUID.randomUUID().toString() + "_" + file.getOriginalFilename());
            
            // Chemin complet
            Path targetLocation = Paths.get(fileStorageConfig.getUploadDir()).resolve(subDirectory).resolve(fileName);
            Files.createDirectories(targetLocation.getParent());

            // Copier le fichier
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file.", ex);
        }
    }

    public Resource loadFileAsResource(String filePath) {
        try {
            System.out.println("=== DÉBUT DU CHARGEMENT DE LA RESSOURCE ===");
            System.out.println("Chemin demandé: " + filePath);
            
            // Nettoyer le chemin du fichier pour éviter les problèmes de sécurité
            filePath = filePath.replace("../", "").replace("..\\", "");
            System.out.println("Chemin nettoyé: " + filePath);
            
            // Construire le chemin complet
            Path fullPath = Paths.get(fileStorageConfig.getUploadDir()).resolve(filePath).normalize();
            System.out.println("Chemin complet: " + fullPath);
            
            // Vérifier si le fichier existe
            if (!Files.exists(fullPath)) {
                System.out.println("Fichier non trouvé: " + fullPath);
                
                // Si c'est une image de livre, essayer de charger l'image par défaut
                if (filePath.contains("book-covers")) {
                    System.out.println("Tentative de chargement de l'image par défaut pour un livre");
                    
                    // Essayer plusieurs chemins possibles pour l'image par défaut
                    String[] defaultPaths = {
                        "book-covers/default-book.jpg",
                        "book-covers/default.jpg",
                        "book-covers/no-cover.jpg",
                        "book-covers/placeholder.jpg"
                    };
                    
                    for (String defaultPath : defaultPaths) {
                        Path defaultImagePath = Paths.get(fileStorageConfig.getUploadDir()).resolve(defaultPath).normalize();
                        System.out.println("Essai du chemin par défaut: " + defaultImagePath);
                        
                        // Vérifier si l'image par défaut existe
                        if (Files.exists(defaultImagePath)) {
                            System.out.println("Image par défaut trouvée: " + defaultImagePath);
                            try {
                                Resource defaultResource = new UrlResource(defaultImagePath.toUri());
                                if (defaultResource.exists() && defaultResource.isReadable()) {
                                    System.out.println("Image par défaut chargée avec succès");
                                    System.out.println("=== FIN DU CHARGEMENT DE LA RESSOURCE ===");
                                    return defaultResource;
                                }
                            } catch (Exception e) {
                                System.out.println("Erreur lors du chargement de l'image par défaut: " + e.getMessage());
                            }
                        }
                    }
                    
                    // Si aucune image par défaut n'a été trouvée, en créer une
                    Path defaultImagePath = Paths.get(fileStorageConfig.getUploadDir()).resolve("book-covers/default-book.jpg").normalize();
                    System.out.println("Aucune image par défaut trouvée, tentative de création: " + defaultImagePath);
                    
                    try {
                        Files.createDirectories(defaultImagePath.getParent());
                        
                        // Créer une image par défaut basique
                        try {
                            // Créer une image basique de 200x300 pixels
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
                            javax.imageio.ImageIO.write(img, "jpg", defaultImagePath.toFile());
                            System.out.println("Image par défaut créée avec succès");
                        } catch (Exception e) {
                            System.out.println("Erreur lors de la création de l'image par défaut: " + e.getMessage());
                            // En cas d'erreur, créer un fichier vide
                            Files.createFile(defaultImagePath);
                            System.out.println("Fichier vide créé comme fallback");
                        }
                        
                        // Essayer de charger l'image par défaut nouvellement créée
                        try {
                            Resource defaultResource = new UrlResource(defaultImagePath.toUri());
                            if (defaultResource.exists() && defaultResource.isReadable()) {
                                System.out.println("Image par défaut nouvellement créée chargée avec succès");
                                System.out.println("=== FIN DU CHARGEMENT DE LA RESSOURCE ===");
                                return defaultResource;
                            }
                        } catch (Exception e) {
                            System.out.println("Erreur lors du chargement de l'image par défaut nouvellement créée: " + e.getMessage());
                        }
                    } catch (IOException e) {
                        System.out.println("Impossible de créer l'image par défaut: " + e.getMessage());
                    }
                }
                
                System.out.println("Aucune ressource trouvée, lancement d'une exception");
                System.out.println("=== FIN DU CHARGEMENT DE LA RESSOURCE (ÉCHEC) ===");
                throw new RuntimeException("File not found: " + filePath);
            }
            
            // Créer la ressource
            Resource resource = new UrlResource(fullPath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                System.out.println("Ressource chargée avec succès: " + resource.getFilename());
                System.out.println("Taille de la ressource: " + resource.contentLength() + " octets");
                System.out.println("=== FIN DU CHARGEMENT DE LA RESSOURCE (SUCCÈS) ===");
                return resource;
            } else {
                System.out.println("La ressource existe mais ne peut pas être chargée: " + filePath);
                System.out.println("=== FIN DU CHARGEMENT DE LA RESSOURCE (ÉCHEC) ===");
                throw new RuntimeException("File exists but cannot be loaded as resource: " + filePath);
            }
        } catch (MalformedURLException ex) {
            System.out.println("URL malformée pour le fichier: " + filePath + " - " + ex.getMessage());
            throw new RuntimeException("File not found: " + filePath, ex);
        } catch (Exception ex) {
            System.out.println("Erreur lors du chargement du fichier: " + filePath + " - " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Error loading file: " + filePath, ex);
        }
    }
    
    /**
     * Vérifie si un fichier existe
     * @param filePath Chemin du fichier relatif au répertoire de téléchargement
     * @return true si le fichier existe, false sinon
     */
    public boolean fileExists(String filePath) {
        try {
            System.out.println("=== VÉRIFICATION DE L'EXISTENCE DU FICHIER ===");
            System.out.println("Chemin demandé: " + filePath);
            
            // Nettoyer le chemin du fichier pour éviter les problèmes de sécurité
            filePath = filePath.replace("../", "").replace("..\\", "");
            System.out.println("Chemin nettoyé: " + filePath);
            
            // Construire le chemin complet
            Path fullPath = Paths.get(fileStorageConfig.getUploadDir()).resolve(filePath).normalize();
            System.out.println("Chemin complet: " + fullPath);
            
            boolean exists = Files.exists(fullPath);
            boolean readable = exists && Files.isReadable(fullPath);
            boolean isRegularFile = exists && Files.isRegularFile(fullPath);
            
            System.out.println("Le fichier " + (exists ? "existe" : "n'existe pas") + ": " + fullPath);
            if (exists) {
                System.out.println("Le fichier est " + (readable ? "lisible" : "non lisible"));
                System.out.println("Le fichier est " + (isRegularFile ? "un fichier régulier" : "non un fichier régulier"));
                
                if (isRegularFile) {
                    try {
                        long size = Files.size(fullPath);
                        System.out.println("Taille du fichier: " + size + " octets");
                        
                        if (size == 0) {
                            System.out.println("ATTENTION: Le fichier existe mais est vide");
                        }
                    } catch (Exception e) {
                        System.out.println("Impossible de déterminer la taille du fichier: " + e.getMessage());
                    }
                }
            }
            
            System.out.println("=== FIN DE LA VÉRIFICATION DE L'EXISTENCE DU FICHIER ===");
            return exists && readable && isRegularFile;
        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification de l'existence du fichier: " + filePath + " - " + e.getMessage());
            e.printStackTrace();
            System.out.println("=== FIN DE LA VÉRIFICATION DE L'EXISTENCE DU FICHIER (ERREUR) ===");
            return false;
        }
    }
}