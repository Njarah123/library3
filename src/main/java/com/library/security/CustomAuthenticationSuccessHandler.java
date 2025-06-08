package com.library.security;

import com.library.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        
        logger.debug("Authentication success for user: {}", authentication.getName());
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        String targetUrl;

        try {
            switch (user.getUserType()) {
                case STAFF:
                    targetUrl = "/staff/dashboard";
                    break;
                case STUDENT:
                    targetUrl = "/student/dashboard";
                    break;
                case LIBRARIAN:
                    targetUrl = "/librarian/dashboard";
                    break;
                default:
                    targetUrl = "/dashboard";
                    break;
            }
            
            logger.debug("Redirecting to: {}", targetUrl);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } catch (Exception e) {
            logger.error("Error during authentication success handling", e);
            response.sendRedirect("/error");
        }
    }
}