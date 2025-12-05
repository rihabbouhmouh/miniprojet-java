package com.eventmanager.security;

import com.eventmanager.entity.User;
import com.eventmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityService {

    @Autowired
    private UserRepository userRepository;

    public Optional<User> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserDetails userDetails) {

            return userRepository.findByEmail(userDetails.getUsername());
        }

        return Optional.empty();
    }

    public boolean isUserLoggedIn() {
        return getAuthenticatedUser().isPresent();
    }

    public boolean hasRole(String role) {
        return getAuthenticatedUser()
                .map(user -> user.getRole().name().equals("ROLE_" + role) ||
                        user.getRole().name().equals(role))
                .orElse(false);
    }
}