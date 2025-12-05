package com.eventmanager.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactiver CSRF pour Vaadin
                .csrf(csrf -> csrf.disable())

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
}
