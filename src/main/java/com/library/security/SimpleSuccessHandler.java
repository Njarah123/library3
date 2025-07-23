package com.library.security;

import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("simpleSuccessHandler")
public class SimpleSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        System.out.println("=== SIMPLE SUCCESS HANDLER ===");
        System.out.println("User: " + authentication.getName());
        System.out.println("Authorities: " + authentication.getAuthorities());
        
        // Redirection simple basée sur le rôle
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        String targetUrl = "/librarian/dashboard"; // Par défaut
        
        switch (role) {
            case "ROLE_STAFF":
                targetUrl = "/staff/dashboard";
                break;
            case "ROLE_STUDENT":
                targetUrl = "/student/dashboard";
                break;
            case "ROLE_LIBRARIAN":
                targetUrl = "/librarian/dashboard";
                break;
        }
        
        System.out.println("Redirecting to: " + targetUrl);
        response.sendRedirect(targetUrl);
    }
}
