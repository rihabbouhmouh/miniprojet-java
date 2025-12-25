package com.eventmanager.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configure SecurityContextRepository to save authentication in session
        SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
        
        http
                // Désactiver CSRF pour Vaadin
                .csrf(csrf -> csrf.disable())
                
                // Configure session management - always create session
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                )
                
                // Set SecurityContextRepository
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository)
                )

                // Autoriser les ressources statiques, H2 console, login, register
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/frontend/**",
                                "/VAADIN/**",
                                "/h2-console/**",
                                "/login",
                                "/register",
                                "/",
                                "/home",
                                "/events",
                                "/event/**"
                        ).permitAll()
                        // Accès CLIENT
                        .requestMatchers("/dashboard/**", "/my-reservations/**", "/profile/**").hasRole("CLIENT")
                        // Accès ORGANIZER
                        .requestMatchers("/organizer/**").hasRole("ORGANIZER")
                        // Accès ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                // Login form
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .permitAll()
                )

                // Logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // Pour la console H2
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
