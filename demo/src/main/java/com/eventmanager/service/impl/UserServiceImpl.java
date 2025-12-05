package com.eventmanager.service.impl;

import com.eventmanager.entity.User;
import com.eventmanager.enums.UserRole;
import com.eventmanager.exception.ConflictException;
import com.eventmanager.exception.ResourceNotFoundException;
import com.eventmanager.repository.EventRepository;
import com.eventmanager.repository.ReservationRepository;
import com.eventmanager.repository.UserRepository;
import com.eventmanager.service.IUserService;
import com.eventmanager.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final SecurityService securityService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           EventRepository eventRepository,
                           ReservationRepository reservationRepository,
                           SecurityService securityService) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.securityService = securityService;
    }

    @Override
    public User registerUser(String nom, String prenom, String email,
                             String password, String telephone, UserRole role) {

        // Vérifier email unique
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Cet email est déjà utilisé");
        }

        // Valider mot de passe
        if (!securityService.isPasswordValid(password)) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
        }

        // Créer utilisateur
        User user = new User();
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        user.setPassword(securityService.hashPassword(password));
        user.setTelephone(telephone);
        user.setRole(role != null ? role : UserRole.CLIENT);
        user.setDateInscription(LocalDateTime.now());
        user.setActif(true);

        return userRepository.save(user);
    }

    @Override
    public Optional<User> authenticate(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> securityService.checkPassword(password, user.getPassword()))
                .filter(User::getActif);
    }

    @Override
    public User updateProfile(Long userId, String nom, String prenom, String telephone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (nom != null && !nom.trim().isEmpty()) {
            user.setNom(nom.trim());
        }

        if (prenom != null && !prenom.trim().isEmpty()) {
            user.setPrenom(prenom.trim());
        }

        if (telephone != null && !telephone.trim().isEmpty()) {
            user.setTelephone(telephone.trim());
        }

        return userRepository.save(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Vérifier ancien mot de passe
        if (!securityService.checkPassword(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Ancien mot de passe incorrect");
        }

        // Valider nouveau mot de passe
        if (!securityService.isPasswordValid(newPassword)) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit contenir au moins 8 caractères");
        }

        // Mettre à jour
        user.setPassword(securityService.hashPassword(newPassword));
        userRepository.save(user);
    }

    @Override
    public User toggleAccountStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        user.setActif(active);
        return userRepository.save(user);
    }

    @Override
    public Map<String, Object> getUserStatistics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Map<String, Object> stats = new HashMap<>();

        // Utilisez findByOrganisateurIdAndStatut
        long eventsCreated = eventRepository.findByOrganisateurIdAndStatut(userId, null).size();
        stats.put("eventsCreated", eventsCreated);

        // Récupérer toutes les réservations et filtrer
        List<com.eventmanager.entity.Reservation> allReservations = reservationRepository.findAll();

        long reservationsCount = allReservations.stream()
                .filter(r -> r.getUtilisateur() != null && r.getUtilisateur().getId().equals(userId))
                .count();
        stats.put("reservationsCount", reservationsCount);

        // Calculer le total dépensé
        Double totalSpent = allReservations.stream()
                .filter(r -> r.getUtilisateur() != null && r.getUtilisateur().getId().equals(userId))
                .mapToDouble(reservation -> reservation.getMontantTotal() != null ? reservation.getMontantTotal() : 0.0)
                .sum();
        stats.put("totalSpent", totalSpent);

        // Compter les réservations confirmées
        long confirmedReservations = allReservations.stream()
                .filter(r -> r.getUtilisateur() != null && r.getUtilisateur().getId().equals(userId))
                .filter(r -> r.getStatut() != null && r.getStatut().toString().equals("CONFIRMEE"))
                .count();
        stats.put("confirmedReservations", confirmedReservations);

        // Événements à venir réservés
        long upcomingReservations = allReservations.stream()
                .filter(r -> r.getUtilisateur() != null && r.getUtilisateur().getId().equals(userId))
                .filter(r -> r.getEvenement() != null &&
                        r.getEvenement().getDateDebut() != null &&
                        r.getEvenement().getDateDebut().isAfter(LocalDateTime.now()))
                .count();
        stats.put("upcomingReservations", upcomingReservations);

        return stats;
    }

    @Override
    public List<User> getUsersWithFilters(String nom, String prenom,
                                          UserRole role, Boolean actif) {
        // CORRECTION SIMPLIFIÉE : Utilisez findAll() et filtrez avec Streams
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(user -> nom == null ||
                        (user.getNom() != null && user.getNom().toLowerCase().contains(nom.toLowerCase())))
                .filter(user -> prenom == null ||
                        (user.getPrenom() != null && user.getPrenom().toLowerCase().contains(prenom.toLowerCase())))
                .filter(user -> role == null || user.getRole() == role)
                .filter(user -> actif == null || user.getActif() == actif)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll();
        }

        String searchTerm = keyword.toLowerCase();
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(user ->
                        (user.getNom() != null && user.getNom().toLowerCase().contains(searchTerm)) ||
                                (user.getPrenom() != null && user.getPrenom().toLowerCase().contains(searchTerm)) ||
                                (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchTerm)))
                .collect(Collectors.toList());
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean isOrganizerOrAdmin(User user) {
        return user.getRole() == UserRole.ORGANIZER || user.getRole() == UserRole.ADMIN;
    }

    @Override
    public Map<UserRole, Long> countUsersByRole() {
        return userRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        User::getRole,
                        Collectors.counting()
                ));
    }

    @Override
    public User updateUserRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        user.setRole(newRole);
        return userRepository.save(user);
    }
}