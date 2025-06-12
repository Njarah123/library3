package com.library.config;

import com.library.security.CustomUserDetailsService;
import com.library.security.CustomAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
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
             .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .authorizeHttpRequests(auth -> auth
            
            .requestMatchers("/home").authenticated()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/auth/**", "/register", "/error").permitAll()
                // Configuration pour les bibliothécaires
                .requestMatchers("/librarian/**").hasRole("LIBRARIAN")
                .requestMatchers("/books/add", "/books/*/edit", "/books/*/delete").hasRole("LIBRARIAN")
                .requestMatchers("/borrowings/approve/**", "/borrowings/reject/**").hasRole("LIBRARIAN")
                .requestMatchers("/books/manage/**").hasRole("LIBRARIAN")
                // Configuration pour le personnel
                .requestMatchers("/admin/**", "/staff/**").hasRole("STAFF")
                .requestMatchers("/reports/**").hasRole("STAFF")
                .requestMatchers("/staff/borrowings/**").hasRole("STAFF")
                .requestMatchers("/staff/reservations/**").hasRole("STAFF")
                // Configuration pour les étudiants
                .requestMatchers("/student/**").hasRole("STUDENT")
                .requestMatchers("/student/borrowings/**").hasRole("STUDENT")
                .requestMatchers("/student/reservations/**").hasRole("STUDENT")
                // Accès commun aux livres
                .requestMatchers("/books", "/books/*", "/search").hasAnyRole("STUDENT", "STAFF", "LIBRARIAN")
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
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/auth/login?logout=true")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("librarySecretKey")
                .tokenValiditySeconds(86400)
                .userDetailsService(userDetailsService)
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