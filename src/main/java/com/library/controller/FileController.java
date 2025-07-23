package com.library.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.library.service.FileStorageService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    private final Map<String, String> mimeTypes = new HashMap<>();

    public FileController() {
        // Initialiser les types MIME courants
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("pdf", "application/pdf");
        mimeTypes.put("doc", "application/msword");
        mimeTypes.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        mimeTypes.put("xls", "application/vnd.ms-excel");
        mimeTypes.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        mimeTypes.put("css", "text/css");
        mimeTypes.put("js", "application/javascript");
        mimeTypes.put("html", "text/html");
        mimeTypes.put("txt", "text/plain");
    }

    /**
     * Endpoint pour servir les images des livres
     * @param filename Nom du fichier image
     * @param request Requête HTTP
     * @return Ressource image avec les en-têtes appropriés
     */
    @GetMapping("/book-covers/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveBookCover(@PathVariable String filename, HttpServletRequest request) {
        System.out.println("=== DÉBUT DE LA DEMANDE D'IMAGE DE COUVERTURE ===");
        System.out.println("Demande d'image de couverture: " + filename);
        
        // Nettoyer et valider le nom du fichier pour éviter les problèmes de sécurité
        if (filename == null || filename.isEmpty() || filename.contains("..")) {
            System.out.println("Nom de fichier invalide ou potentiellement dangereux: " + filename);
            return serveDefaultBookCover(request);
        }
        
        filename = sanitizeFilename(filename);
        System.out.println("Nom de fichier nettoyé: " + filename);
        
        try {
            // Essayer plusieurs chemins possibles pour l'image
            Resource resource = null;
            String[] possiblePaths = {
                // Chemins dans le répertoire book-covers
                "book-covers/" + filename,
                "book-covers/" + filename.toLowerCase(),
                "book-covers/" + filename.replace(" ", "_"),
                "book-covers/" + filename.replace("_", " "),
                "book-covers/" + filename.replace("-", "_"),
                "book-covers/" + filename.replace("_", "-"),
                
                // Chemins dans le répertoire images
                "images/" + filename,
                "images/" + filename.toLowerCase(),
                "images/" + filename.replace(" ", "_"),
                "images/" + filename.replace("_", " "),
                
                // Chemins dans le répertoire static/images
                "static/images/" + filename,
                "static/images/" + filename.toLowerCase(),
                
                // Chemins absolus (convertis en relatifs)
                filename.startsWith("/") ? filename.substring(1) : filename,
                filename.startsWith("/") ? "images/" + filename.substring(filename.lastIndexOf('/') + 1) : "images/" + filename
            };
            
            System.out.println("Tentative de chargement avec " + possiblePaths.length + " chemins possibles");
            
            // Essayer chaque chemin possible
            for (String path : possiblePaths) {
                System.out.println("Essai du chemin: " + path);
                if (fileStorageService.fileExists(path)) {
                    try {
                        resource = fileStorageService.loadFileAsResource(path);
                        if (resource != null && resource.exists() && isValidImageFile(resource)) {
                            System.out.println("Image valide trouvée à: " + path);
                            break;
                        } else {
                            if (resource == null) {
                                System.out.println("Ressource null pour le chemin: " + path);
                            } else if (!resource.exists()) {
                                System.out.println("La ressource n'existe pas pour le chemin: " + path);
                            } else {
                                System.out.println("Le fichier n'est pas une image valide pour le chemin: " + path);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Erreur lors du chargement de " + path + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Le fichier n'existe pas pour le chemin: " + path);
                }
            }
            
            // Si aucune image valide n'a été trouvée, utiliser l'image par défaut
            if (resource == null || !resource.exists()) {
                System.out.println("Aucune image valide trouvée, utilisation de l'image par défaut");
                return serveDefaultBookCover(request);
            }
            
            // Déterminer le type de contenu
            String contentType = determineContentType(resource, request);
            System.out.println("Type de contenu déterminé: " + contentType);
            
            // Vérifier si le type de contenu est une image
            if (!contentType.startsWith("image/")) {
                System.out.println("Le fichier n'est pas une image valide (type: " + contentType + "), utilisation de l'image par défaut");
                return serveDefaultBookCover(request);
            }
            
            // Construire la réponse avec les en-têtes appropriés
            System.out.println("Envoi de l'image avec le type de contenu: " + contentType);
            System.out.println("=== FIN DE LA DEMANDE D'IMAGE DE COUVERTURE ===");
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400") // Cache pendant 24h
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*") // Permettre l'accès depuis n'importe quelle origine
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'image: " + filename + " - " + e.getMessage());
            return serveDefaultBookCover(request);
        }
    }
    
    /**
     * Sert l'image de couverture par défaut
     * @param request Requête HTTP
     * @return Réponse HTTP avec l'image par défaut
     */
    private ResponseEntity<Resource> serveDefaultBookCover(HttpServletRequest request) {
        try {
            System.out.println("=== DÉBUT DU CHARGEMENT DE L'IMAGE PAR DÉFAUT ===");
            System.out.println("Tentative de chargement de l'image par défaut");
            
            // Essayer plusieurs chemins possibles pour l'image par défaut
            String[] defaultPaths = {
                "book-covers/default-book.jpg",
                "book-covers/default.jpg",
                "book-covers/no-cover.jpg",
                "book-covers/placeholder.jpg"
            };
            
            Resource defaultResource = null;
            
            for (String path : defaultPaths) {
                System.out.println("Essai du chemin par défaut: " + path);
                if (fileStorageService.fileExists(path)) {
                    try {
                        defaultResource = fileStorageService.loadFileAsResource(path);
                        if (defaultResource != null && defaultResource.exists() && isValidImageFile(defaultResource)) {
                            System.out.println("Image par défaut valide trouvée à: " + path);
                            break;
                        }
                    } catch (Exception e) {
                        System.out.println("Erreur lors du chargement de " + path + ": " + e.getMessage());
                    }
                }
            }
            
            if (defaultResource != null && defaultResource.exists() && isValidImageFile(defaultResource)) {
                System.out.println("Envoi de l'image par défaut");
                System.out.println("=== FIN DU CHARGEMENT DE L'IMAGE PAR DÉFAUT ===");
                
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"default-book.jpg\"")
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=86400") // Cache pendant 24h
                        .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*") // Permettre l'accès depuis n'importe quelle origine
                        .body(defaultResource);
            } else {
                System.out.println("L'image par défaut n'est pas valide ou n'existe pas");
                System.out.println("Création d'une image transparente");
                
                // Créer une réponse avec une image vide transparente en base64
                String transparentPixel = "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7"; // GIF 1x1 transparent
                byte[] imageBytes = java.util.Base64.getDecoder().decode(transparentPixel);
                org.springframework.core.io.ByteArrayResource emptyImage = new org.springframework.core.io.ByteArrayResource(imageBytes);
                
                System.out.println("Envoi d'une image transparente");
                System.out.println("=== FIN DU CHARGEMENT DE L'IMAGE PAR DÉFAUT ===");
                
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_GIF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"empty.gif\"")
                        .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*") // Permettre l'accès depuis n'importe quelle origine
                        .body(emptyImage);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Impossible de charger l'image par défaut: " + ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Nettoie le nom de fichier pour éviter les problèmes de sécurité
     * @param filename Nom de fichier à nettoyer
     * @return Nom de fichier nettoyé
     */
    private String sanitizeFilename(String filename) {
        // Supprimer les caractères potentiellement dangereux
        return filename
                .replace("../", "")
                .replace("..\\", "")
                .replace(";", "")
                .replace("&", "")
                .replace("|", "")
                .replace("*", "")
                .replace("?", "")
                .replace("<", "")
                .replace(">", "")
                .replace("\"", "")
                .replace("'", "")
                .replace("`", "");
    }
    
    /**
     * Vérifie si un fichier est une image valide
     * @param resource Ressource à vérifier
     * @return true si c'est une image valide, false sinon
     */
    private boolean isValidImageFile(Resource resource) {
        try {
            if (resource == null || !resource.exists() || resource.contentLength() == 0) {
                return false;
            }
            
            String filename = resource.getFilename();
            if (filename == null) {
                return false;
            }
            
            // Vérifier l'extension
            String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            if (!mimeTypes.containsKey(extension) || !mimeTypes.get(extension).startsWith("image/")) {
                return false;
            }
            
            // Vérifier les premiers octets pour s'assurer que c'est bien une image
            byte[] header = new byte[8];
            try (java.io.InputStream is = resource.getInputStream()) {
                int bytesRead = is.read(header);
                if (bytesRead < 2) {
                    return false;
                }
                
                // Vérifier les signatures de fichiers d'images courantes
                if ((header[0] == (byte)0xFF && header[1] == (byte)0xD8) || // JPEG
                    (header[0] == (byte)0x89 && header[1] == (byte)0x50) || // PNG
                    (header[0] == (byte)0x47 && header[1] == (byte)0x49) || // GIF
                    (header[0] == (byte)0x42 && header[1] == (byte)0x4D)) { // BMP
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification de l'image: " + e.getMessage());
            return false;
        }
    }

    /**
     * Endpoint pour servir les images directement depuis le répertoire images
     * @param filename Nom du fichier image
     * @param request Requête HTTP
     * @return Ressource image avec les en-têtes appropriés
     */
    @GetMapping("/images/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveImageFile(@PathVariable String filename, HttpServletRequest request) {
        System.out.println("=== DÉBUT DE LA DEMANDE D'IMAGE DEPUIS /images/ ===");
        System.out.println("Demande d'image: " + filename);
        
        // Nettoyer et valider le nom du fichier pour éviter les problèmes de sécurité
        if (filename == null || filename.isEmpty() || filename.contains("..")) {
            System.out.println("Nom de fichier invalide ou potentiellement dangereux: " + filename);
            return serveDefaultBookCover(request);
        }
        
        filename = sanitizeFilename(filename);
        System.out.println("Nom de fichier nettoyé: " + filename);
        
        try {
            // Essayer de charger l'image depuis le répertoire images
            Resource resource = null;
            String path = "images/" + filename;
            
            System.out.println("Tentative de chargement depuis: " + path);
            if (fileStorageService.fileExists(path)) {
                resource = fileStorageService.loadFileAsResource(path);
                if (resource != null && resource.exists() && isValidImageFile(resource)) {
                    System.out.println("Image valide trouvée à: " + path);
                    
                    // Déterminer le type de contenu
                    String contentType = determineContentType(resource, request);
                    System.out.println("Type de contenu déterminé: " + contentType);
                    
                    // Construire la réponse avec les en-têtes appropriés
                    System.out.println("Envoi de l'image avec le type de contenu: " + contentType);
                    System.out.println("=== FIN DE LA DEMANDE D'IMAGE DEPUIS /images/ ===");
                    
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                            .header(HttpHeaders.CACHE_CONTROL, "max-age=86400") // Cache pendant 24h
                            .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*") // Permettre l'accès depuis n'importe quelle origine
                            .body(resource);
                }
            }
            
            // Si l'image n'a pas été trouvée, essayer avec l'image par défaut
            System.out.println("Image non trouvée dans le répertoire images, utilisation de l'image par défaut");
            return serveDefaultBookCover(request);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'image: " + filename + " - " + e.getMessage());
            return serveDefaultBookCover(request);
        }
    }

    /**
     * Endpoint pour servir les fichiers généraux (CSS, JS, etc.)
     * @param filename Nom du fichier
     * @param request Requête HTTP
     * @return Ressource fichier avec les en-têtes appropriés
     */
    @GetMapping("/static/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveStaticFile(@PathVariable String filename, HttpServletRequest request) {
        try {
            // Charger le fichier comme ressource
            Resource resource = fileStorageService.loadFileAsResource("static/" + filename);
            
            // Déterminer le type de contenu
            String contentType = determineContentType(resource, request);
            
            // Construire la réponse avec les en-têtes appropriés
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Détermine le type MIME d'un fichier
     * @param resource Ressource fichier
     * @param request Requête HTTP
     * @return Type MIME du fichier
     */
    private String determineContentType(Resource resource, HttpServletRequest request) {
        String contentType = null;
        try {
            // Essayer de déterminer le type de contenu à partir de l'extension du fichier
            String filename = resource.getFilename();
            if (filename != null) {
                String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
                contentType = mimeTypes.getOrDefault(extension, null);
            }
            
            // Si le type n'a pas été déterminé à partir de l'extension, essayer de le détecter
            if (contentType == null) {
                Path path = resource.getFile().toPath();
                contentType = Files.probeContentType(path);
            }
            
            // Si toujours pas de type détecté, utiliser le type par défaut du serveur
            if (contentType == null) {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            }
        } catch (IOException ex) {
            // En cas d'erreur, utiliser un type générique
            System.out.println("Could not determine file type: " + ex.getMessage());
        }
        
        // Si aucun type n'a été déterminé, utiliser application/octet-stream comme fallback
        return contentType != null ? contentType : "application/octet-stream";
    }
}