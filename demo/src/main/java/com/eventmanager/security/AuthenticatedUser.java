package com.eventmanager.security;

import com.eventmanager.entity.User;
import com.eventmanager.repository.UserRepository;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class AuthenticatedUser {

    private final UserRepository userRepository;
    private final AuthenticationContext authenticationContext;

    public AuthenticatedUser(AuthenticationContext authenticationContext,
                             UserRepository userRepository) {
        this.userRepository = userRepository;
        this.authenticationContext = authenticationContext;
    }

    @Transactional
    public Optional<User> get() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .flatMap(userDetails -> userRepository.findByEmail(userDetails.getUsername()));
    }

    public void logout() {
        authenticationContext.logout();
    }

    public boolean isAuthenticated() {
        return authenticationContext.isAuthenticated();
    }

    public boolean hasRole(String role) {
        return get().map(user -> user.getRole().name().equals(role)).orElse(false);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isOrganizer() {
        return hasRole("ORGANIZER");
    }

    public boolean isClient() {
        return hasRole("CLIENT");
    }

    public Optional<String> getEmail() {
        return get().map(User::getEmail);
    }

    public String getFullName() {
        return get()
                .map(user -> user.getPrenom() + " " + user.getNom())
                .orElse("Utilisateur");
    }

    public Optional<Long> getUserId() {
        return get().map(User::getId);
    }
}