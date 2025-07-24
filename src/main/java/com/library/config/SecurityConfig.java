package com.library.config;

import com.library.security.CustomUserDetailsService;
import com.library.security.CustomAuthenticationSuccessHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ConditionalOnProperty(name = "security.robust.enabled", havingValue = "false", matchIfMissing = false)
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler,
                         CustomUserDetailsService userDetailsService) {
        this.successHandler = successHandler;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configuration des sessions - Créer les sessions immédiatement
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                .maximumSessions(2)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/auth/login?expired=true")
                .and()
                .sessionFixation().newSession()
                .enableSessionUrlRewriting(false)
            )
            
            // Configuration CSRF sélective
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(
                    // Pages publiques sans CSRF
                    "/", "/home",
                    "/auth/**", "/register",
                    // Ressources statiques
                    "/css/**", "/js/**", "/images/**", "/uploads/**",
                    // API endpoints pour les images de profil
                    "/api/users/*/profile-image", "/api/users/*/has-profile-image",
                    // Autres endpoints
                    "/error", "/notifications/mark-as-read"
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/home", "/info/**").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                .requestMatchers("/api/users/*/profile-image", "/api/users/*/has-profile-image").permitAll()
                .requestMatchers("/auth/**", "/register", "/error", "/auth/login-remembered", "/auth/remove-remembered-user", "/notifications/**").permitAll()
                .requestMatchers("/diagnostic/**").permitAll() // Permettre l'accès aux endpoints de diagnostic
                .requestMatchers("/auth/profile-photo-setup").permitAll() // Permettre l'accès à la configuration de photo de profil
                .requestMatchers("/librarian/**").hasRole("LIBRARIAN")
                .requestMatchers("/books/add", "/books/*/edit", "/books/*/delete").hasRole("LIBRARIAN")
                .requestMatchers("/borrowings/approve/**", "/borrowings/reject/**").hasRole("LIBRARIAN")
                .requestMatchers("/books/manage/**").hasRole("LIBRARIAN")
                .requestMatchers("/admin/**", "/staff/**").hasRole("STAFF")
                .requestMatchers("/reports/**").hasRole("STAFF")
                .requestMatchers("/staff/borrowings/**").hasRole("STAFF")
                .requestMatchers("/staff/reservations/**").hasRole("STAFF")
                .requestMatchers("/student/**").hasRole("STUDENT")
                .requestMatchers("/student/borrowings/**").hasRole("STUDENT")
                .requestMatchers("/student/reservations/**").hasRole("STUDENT")
                .requestMatchers("/books", "/books/*", "/search").hasAnyRole("STUDENT", "STAFF", "LIBRARIAN")
                .requestMatchers("/payments/**").hasAnyRole("STUDENT", "STAFF", "LIBRARIAN")
                .requestMatchers("/profile/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .successHandler(successHandler)
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .logoutSuccessUrl("/auth/login?logout")
                .deleteCookies("JSESSIONID", "remember-me")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .rememberMe(rememberMe -> rememberMe
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400 * 30) // 30 days
                .userDetailsService(userDetailsService)
                .rememberMeParameter("remember-me")
            );
           

        return http.build();
    }
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
