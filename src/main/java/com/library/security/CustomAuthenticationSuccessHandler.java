package com.library.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
   
   private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        logger.debug("Authentication success for user: {}", authentication.getName());
        
        try {
            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(1800);
            session.setAttribute("username", authentication.getName());
            session.setAttribute("authenticated", true);
            session.setAttribute("loginTime", System.currentTimeMillis());
            
            String targetUrl = "/dashboard";
            
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                User user = userDetails.getUser();
                
                if (user != null) {
                    session.setAttribute("currentUser", user);
                    session.setAttribute("userType", user.getUserType().toString());
                    targetUrl = determineTargetUrl(user);
                    
                    if (request.getParameter("remember-me") != null) {
                        updateRememberedUsersCookie(request, response, user);
                    }
                    
                    logger.info("User type: {}", user.getUserType());
                } else {
                    logger.error("User object is null for principal: {}", authentication.getName());
                }
            } else {
                logger.error("Principal is not instance of CustomUserDetails: {}",
                    authentication.getPrincipal().getClass().getName());
                targetUrl = determineTargetUrlByRole(authentication);
            }
            
            logger.info("User {} successfully authenticated", authentication.getName());
            logger.info("Session ID: {}", session.getId());
            logger.info("Redirecting to: {}", targetUrl);
            
            response.sendRedirect(targetUrl);
            
        } catch (Exception e) {
            logger.error("Error during authentication success handling for user: {}",
                authentication.getName(), e);
            response.sendRedirect("/auth/login?error=redirect_failed");
        }
    }

    private void updateRememberedUsersCookie(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> rememberedUsers = new ArrayList<>();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("remembered-users".equals(cookie.getName())) {
                    String decodedValue = new String(Base64.getUrlDecoder().decode(cookie.getValue()));
                    rememberedUsers = mapper.readValue(decodedValue, new TypeReference<List<Map<String, String>>>() {});
                    break;
                }
            }
        }

        Optional<Map<String, String>> existingUser = rememberedUsers.stream()
            .filter(u -> u.get("username").equals(user.getUsername()))
            .findFirst();

        if (existingUser.isPresent()) {
            existingUser.get().put("name", user.getName());
            existingUser.get().put("profileImage", user.getProfileImagePath() != null ? user.getProfileImagePath() : "/uploads/profiles/default-profile.png");
            existingUser.get().put("userType", user.getUserType().toString());
        } else {
            rememberedUsers.add(Map.of(
                "username", user.getUsername(),
                "name", user.getName(),
                "profileImage", user.getProfileImagePath() != null ? user.getProfileImagePath() : "/uploads/profiles/default-profile.png",
                "userType", user.getUserType().toString()
            ));
        }

        String jsonValue = mapper.writeValueAsString(rememberedUsers);
        String encodedValue = Base64.getUrlEncoder().encodeToString(jsonValue.getBytes());
        
        Cookie cookie = new Cookie("remembered-users", encodedValue);
        cookie.setMaxAge(86400 * 30); // 30 jours
        cookie.setPath("/");
        response.addCookie(cookie);
    }
    
    /**
     * Détermine l'URL de redirection basée sur le type d'utilisateur
     */
    private String determineTargetUrl(User user) {
        switch (user.getUserType()) {
            case STAFF:
                return "/staff/dashboard";
            case STUDENT:
                return "/student/dashboard";
            case LIBRARIAN:
                return "/librarian/dashboard";
            default:
                logger.warn("Unknown user type: {} for user: {}", user.getUserType(), user.getUsername());
                return "/dashboard";
        }
    }
    
    /**
     * Fallback: détermine l'URL basée sur les rôles Spring Security
     */
    private String determineTargetUrlByRole(Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        logger.info("Determining URL by role: {}", role);
        
        switch (role) {
            case "ROLE_STAFF":
                return "/staff/dashboard";
            case "ROLE_STUDENT":
                return "/student/dashboard";
            case "ROLE_LIBRARIAN":
                return "/librarian/dashboard";
            default:
                logger.warn("Unknown role: {}", role);
                return "/dashboard";
        }
    }
}
