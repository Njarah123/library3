package com.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.logging.Logger;

@Controller
@RequestMapping("/error")
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    private static final Logger logger = Logger.getLogger(ErrorController.class.getName());
    
    @Autowired
    private ErrorAttributes errorAttributes;

    /**
     * Affiche la page d'erreur pour les problèmes d'emprunt
     */
    @GetMapping("/borrow")
    public String showBorrowError(Model model) {
        // Si le message d'erreur n'est pas déjà défini (via redirectAttributes), utiliser un message par défaut
        if (!model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", "Une erreur s'est produite lors de l'emprunt. Veuillez réessayer.");
        }
        return "error/borrow-error";
    }
    
    /**
     * Gère les erreurs liées aux images
     */
    @GetMapping("/image")
    public String handleImageError(Model model, HttpServletRequest request) {
        String imagePath = request.getParameter("path");
        logger.info("Erreur d'image détectée pour le chemin: " + imagePath);
        
        model.addAttribute("errorMessage", "L'image demandée n'a pas pu être chargée.");
        model.addAttribute("imagePath", imagePath);
        model.addAttribute("suggestion", "Veuillez contacter l'administrateur si le problème persiste.");
        
        return "error/image-error";
    }
    
    /**
     * Gère les erreurs générales
     */
    @RequestMapping
    public String handleError(Model model, HttpServletRequest request) {
        // Créer une WebRequest à partir de HttpServletRequest
        WebRequest webRequest = new ServletWebRequest(request);
        
        // Récupérer les attributs d'erreur
        Map<String, Object> errorAttributes = getErrorAttributes(webRequest, true);
        
        // Journaliser l'erreur
        logger.warning("Erreur détectée: " + errorAttributes.getOrDefault("message", "Inconnue") +
                      " (status: " + errorAttributes.getOrDefault("status", "Inconnu") + ")");
        
        // Ajouter les attributs au modèle
        model.addAttribute("status", errorAttributes.getOrDefault("status", 500));
        model.addAttribute("error", errorAttributes.getOrDefault("error", "Erreur inconnue"));
        model.addAttribute("message", errorAttributes.getOrDefault("message", "Une erreur s'est produite"));
        model.addAttribute("path", errorAttributes.getOrDefault("path", request.getRequestURI()));
        
        // Déterminer le type d'erreur
        Integer status = (Integer) errorAttributes.getOrDefault("status", 500);
        if (status == 404) {
            return "error/404";
        } else if (status == 403) {
            return "error/403";
        } else if (status == 500) {
            return "error/500";
        }
        
        return "error/general";
    }
    
    /**
     * Récupère les attributs d'erreur
     */
    private Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
        ErrorAttributeOptions options = ErrorAttributeOptions.defaults();
        
        if (includeStackTrace) {
            options = options.including(ErrorAttributeOptions.Include.STACK_TRACE);
        }
        
        try {
            return errorAttributes.getErrorAttributes(webRequest, options);
        } catch (Exception e) {
            logger.severe("Erreur lors de la récupération des attributs d'erreur: " + e.getMessage());
            return Map.of(
                "status", 500,
                "error", "Internal Server Error",
                "message", "Une erreur s'est produite lors du traitement de la demande"
            );
        }
    }
}