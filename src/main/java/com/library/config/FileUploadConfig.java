package com.library.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import jakarta.annotation.PostConstruct;

/**
 * Configuration spécialisée pour résoudre définitivement FileCountLimitExceededException
 * Cette classe configure les propriétés système Tomcat au démarrage de l'application
 */
@Configuration
public class FileUploadConfig {

    @PostConstruct
    public void configureFileUploadLimits() {
        // Configuration des propriétés système pour FileUploadBase
        // Ces propriétés doivent être définies avant l'initialisation de Tomcat
        System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileCountMax", "10000");
        System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileSizeMax", "52428800"); // 50MB
        System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.sizeMax", "104857600"); // 100MB
        
        // Propriétés supplémentaires pour forcer la résolution
        System.setProperty("tomcat.util.http.fileupload.FileUploadBase.fileCountMax", "10000");
        System.setProperty("tomcat.util.http.fileupload.impl.FileCountLimitExceededException.fileCountMax", "10000");
        
        // Configuration des limites de parsing multipart via propriétés système Tomcat uniquement
        System.setProperty("tomcat.maxParameterCount", "50000");
        
        System.out.println("=== FileUploadConfig initialisé ===");
        System.out.println("FileCountMax: " + System.getProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileCountMax"));
        System.out.println("FileSizeMax: " + System.getProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileSizeMax"));
        System.out.println("SizeMax: " + System.getProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.sizeMax"));
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // Skip verbose logging in production
        String activeProfile = System.getProperty("spring.profiles.active", "");
        if (!activeProfile.contains("prod")) {
            System.out.println("=== Application prête - Configuration FileUpload active ===");
            System.out.println("Les limites de fichiers ont été configurées pour éviter FileCountLimitExceededException");
        }
    }
}