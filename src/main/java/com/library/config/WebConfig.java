package com.library.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.MultipartConfigElement;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
        registry.addResourceHandler("/static/uploads/**")
                .addResourceLocations("classpath:/static/uploads/");
        
        // Ajouter un gestionnaire pour le chemin /uploads/**
        // Servir à la fois depuis le classpath et depuis le système de fichiers
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                    "classpath:/static/uploads/",  // Pour les images par défaut
                    "file:./uploads/",             // Pour les fichiers dans ./uploads/
                    "file:./uploads/profiles/",    // Pour les profils dans ./uploads/profiles/
                    "file:./uploads/books/",       // Pour les fichiers dans ./uploads/books/
                    "file:./uploads/books/profiles/" // Pour les profils dans ./uploads/books/profiles/
                );
        
        // Ajouter un gestionnaire pour le chemin /files/**
        // Ce chemin sera utilisé par le FileController
        registry.addResourceHandler("/files/**")
                .addResourceLocations(
                    "file:./uploads/",             // Pour les fichiers dans ./uploads/
                    "classpath:/static/images/"    // Pour les images par défaut dans les ressources statiques
                )
                .setCachePeriod(3600)              // Mettre en cache pendant 1 heure
                .resourceChain(true);              // Activer la chaîne de ressources
    }
    
    /**
     * Configuration Tomcat spécialisée pour éviter FileCountLimitExceededException
     * Approche ciblée avec gestion des propriétés système
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addConnectorCustomizers(this::customizeConnector);
        return tomcat;
    }
    
    private void customizeConnector(Connector connector) {
        // Configuration spécifique pour éviter FileCountLimitExceededException
        // Définir des limites très élevées pour éliminer complètement l'erreur
        connector.setMaxPostSize(100 * 1024 * 1024); // 100MB
        connector.setMaxSavePostSize(100 * 1024 * 1024); // 100MB
        
        // Propriétés système critiques pour FileCountLimitExceededException
        // Utilisation de limites très élevées pour éviter toute restriction
        System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileCountMax", "10000");
        System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileSizeMax", "52428800"); // 50MB
        System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.sizeMax", "104857600"); // 100MB
        
        // Configuration des propriétés du connecteur avec limites très élevées
        connector.setProperty("maxParameterCount", "50000");
        connector.setProperty("maxHttpHeaderSize", "131072"); // 128KB
        connector.setProperty("maxSwallowSize", "104857600"); // 100MB
        
        // Configuration spécifique pour les requêtes multipart
        connector.setProperty("allowCasualMultipartParsing", "true");
        connector.setProperty("maxFileCount", "10000");
        connector.setProperty("maxFormKeys", "50000");
        connector.setProperty("maxFormSize", "104857600"); // 100MB
        
        // Propriétés supplémentaires pour forcer la résolution
        System.setProperty("tomcat.util.http.fileupload.FileUploadBase.fileCountMax", "10000");
        System.setProperty("tomcat.util.http.fileupload.impl.FileCountLimitExceededException.fileCountMax", "10000");
        
        // Log de la configuration
        System.out.println("Tomcat Connector configuré avec limites très élevées pour éliminer FileCountLimitExceededException");
        System.out.println("FileCountMax: 10000, FileSizeMax: 50MB, SizeMax: 100MB");
    }
    
    /**
     * Configuration MultipartResolver avec gestion d'erreur
     */
    @Bean
    public MultipartResolver multipartResolver() {
        org.springframework.web.multipart.support.StandardServletMultipartResolver resolver = 
            new org.springframework.web.multipart.support.StandardServletMultipartResolver();
        resolver.setResolveLazily(false);
        return resolver;
    }
}