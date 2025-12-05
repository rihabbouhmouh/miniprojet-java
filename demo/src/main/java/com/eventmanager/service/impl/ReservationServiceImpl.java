package com.eventmanager.service.impl;

import com.eventmanager.entity.Event;
import com.eventmanager.entity.Reservation;
import com.eventmanager.entity.User;
import com.eventmanager.enums.ReservationStatus;
import com.eventmanager.exception.BusinessException;
import com.eventmanager.exception.ResourceNotFoundException;
import com.eventmanager.repository.EventRepository;
import com.eventmanager.repository.ReservationRepository;
import com.eventmanager.repository.UserRepository;
import com.eventmanager.service.IEventService;
import com.eventmanager.service.IReservationService;
import com.eventmanager.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationServiceImpl implements IReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final IEventService eventService;
    private final IUserService userService;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  UserRepository userRepository,
                                  EventRepository eventRepository,
                                  IEventService eventService,
                                  IUserService userService) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.eventService = eventService;
        this.userService = userService;
    }

    @Override
    public Reservation createReservation(Long userId, Long eventId, int nombrePlaces) {
        User user = userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);

        // Vérifier si l'événement est disponible
        if (eventService.isEventFull(event.getId())) {
            throw new BusinessException("L'événement est complet");
        }

        // Vérifier les places disponibles
        int availableSeats = eventService.getAvailableSeats(event);
        if (nombrePlaces > availableSeats) {
            throw new BusinessException("Nombre de places insuffisant. Places disponibles: " + availableSeats);
        }

        // Vérifier si l'événement est à venir
        if (event.getDateDebut().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Impossible de réserver pour un événement passé");
        }

        // Créer la réservation
        Reservation reservation = new Reservation();
        reservation.setUtilisateur(user);
        reservation.setEvenement(event);
        reservation.setNombrePlaces(nombrePlaces);
        reservation.setStatut(ReservationStatus.EN_ATTENTE);

        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation updateReservationStatus(Long reservationId, ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        if (reservation.getStatut() == ReservationStatus.ANNULEE) {
            throw new BusinessException("Impossible de modifier une réservation annulée");
        }

        reservation.setStatut(status);

        if (status == ReservationStatus.CONFIRMEE) {
            Event event = reservation.getEvenement();
            int availableSeats = eventService.getAvailableSeats(event);
            if (reservation.getNombrePlaces() > availableSeats) {
                throw new BusinessException("Pas assez de places disponibles pour confirmer cette réservation");
            }
        }

        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));
    }

    @Override
    public List<Reservation> getReservationsByUser(Long userId) {
        userService.getUserById(userId);

        try {
            return reservationRepository.findByUtilisateurId(userId);
        } catch (Exception e) {
            return reservationRepository.findAll()
                    .stream()
                    .filter(r -> r.getUtilisateur() != null && r.getUtilisateur().getId().equals(userId))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<Reservation> getReservationsByEvent(Long eventId) {
        Event event = eventService.getEventById(eventId);

        try {
            return reservationRepository.findByEvenementId(eventId);
        } catch (Exception e) {
            return reservationRepository.findAll()
                    .stream()
                    .filter(r -> r.getEvenement() != null && r.getEvenement().getId().equals(eventId))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<Reservation> getReservationsByStatus(ReservationStatus status) {
        try {
            return reservationRepository.findByStatut(status);
        } catch (Exception e) {
            return reservationRepository.findAll()
                    .stream()
                    .filter(r -> r.getStatut() == status)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        if (reservation.getStatut() == ReservationStatus.ANNULEE) {
            throw new BusinessException("La réservation est déjà annulée");
        }

        if (reservation.getEvenement().getDateDebut().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Impossible d'annuler une réservation pour un événement déjà commencé");
        }

        reservation.setStatut(ReservationStatus.ANNULEE);
        String commentaire = reservation.getCommentaire() != null ?
                reservation.getCommentaire() + " | Annulée le " + LocalDateTime.now() :
                "Annulée le " + LocalDateTime.now();
        reservation.setCommentaire(commentaire);

        reservationRepository.save(reservation);
    }

    @Override
    public Reservation confirmReservation(Long reservationId) {
        return updateReservationStatus(reservationId, ReservationStatus.CONFIRMEE);
    }

    @Override
    public boolean checkEventAvailability(Long eventId, int requestedSeats) {
        Event event = eventService.getEventById(eventId);
        int availableSeats = eventService.getAvailableSeats(event);
        return availableSeats >= requestedSeats;
    }

    @Override
    public int getAvailableSeats(Long eventId) {
        Event event = eventService.getEventById(eventId);
        return eventService.getAvailableSeats(event);
    }

    @Override
    public double calculateReservationAmount(Long reservationId) {
        Reservation reservation = getReservationById(reservationId);
        return reservation.getMontantTotal() != null ? reservation.getMontantTotal() : 0.0;
    }

    @Override
    public List<Reservation> getUserReservationsWithDetails(Long userId) {
        return getReservationsByUser(userId);
    }

    @Override
    public Map<String, Object> getReservationStatistics(Long eventId) {
        Event event = eventService.getEventById(eventId);
        List<Reservation> reservations = getReservationsByEvent(eventId);

        Map<String, Object> stats = new java.util.HashMap<>();

        stats.put("totalReservations", reservations.size());

        Map<ReservationStatus, Long> reservationsByStatus = reservations.stream()
                .collect(Collectors.groupingBy(
                        Reservation::getStatut,
                        Collectors.counting()
                ));
        stats.put("reservationsByStatus", reservationsByStatus);

        int totalPlacesReserved = reservations.stream()
                .mapToInt(Reservation::getNombrePlaces)
                .sum();
        stats.put("totalPlacesReserved", totalPlacesReserved);

        double totalAmount = reservations.stream()
                .mapToDouble(r -> r.getMontantTotal() != null ? r.getMontantTotal() : 0)
                .sum();
        stats.put("totalAmount", totalAmount);

        double fillRate = event.getCapaciteMax() > 0 ?
                (double) totalPlacesReserved / event.getCapaciteMax() * 100 : 0;
        stats.put("fillRate", Math.round(fillRate * 100.0) / 100.0);

        return stats;
    }

    @Override
    public List<Reservation> searchReservations(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return reservationRepository.findAll();
        }

        String searchTerm = keyword.toLowerCase();
        return reservationRepository.findAll()
                .stream()
                .filter(r ->
                        (r.getUtilisateur() != null &&
                                ((r.getUtilisateur().getNom() != null && r.getUtilisateur().getNom().toLowerCase().contains(searchTerm)) ||
                                        (r.getUtilisateur().getPrenom() != null && r.getUtilisateur().getPrenom().toLowerCase().contains(searchTerm)) ||
                                        (r.getUtilisateur().getEmail() != null && r.getUtilisateur().getEmail().toLowerCase().contains(searchTerm)))) ||
                                (r.getEvenement() != null &&
                                        r.getEvenement().getTitre() != null && r.getEvenement().getTitre().toLowerCase().contains(searchTerm)) ||
                                (r.getCodeReservation() != null && r.getCodeReservation().toLowerCase().contains(searchTerm))
                )
                .collect(Collectors.toList());
    }

    @Override
    public Reservation updateReservation(Long reservationId, int newNombrePlaces) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée"));

        if (reservation.getStatut() == ReservationStatus.ANNULEE) {
            throw new BusinessException("Impossible de modifier une réservation annulée");
        }

        if (reservation.getStatut() == ReservationStatus.CONFIRMEE &&
                reservation.getEvenement().getDateDebut().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Impossible de modifier une réservation confirmée pour un événement déjà commencé");
        }

        Event event = reservation.getEvenement();
        int difference = newNombrePlaces - reservation.getNombrePlaces();

        if (difference > 0) {
            int availableSeats = eventService.getAvailableSeats(event);
            if (difference > availableSeats) {
                throw new BusinessException("Pas assez de places disponibles");
            }
        }

        reservation.setNombrePlaces(newNombrePlaces);

        return reservationRepository.save(reservation);
    }

    public List<Reservation> getActiveReservationsForEvent(Long eventId) {
        return getReservationsByEvent(eventId)
                .stream()
                .filter(r -> r.getStatut() == ReservationStatus.EN_ATTENTE ||
                        r.getStatut() == ReservationStatus.CONFIRMEE)
                .collect(Collectors.toList());
    }

    public void processExpiredReservations() {
        LocalDateTime expirationTime = LocalDateTime.now().minusHours(24);

        reservationRepository.findAll()
                .stream()
                .filter(r -> r.getStatut() == ReservationStatus.EN_ATTENTE &&
                        r.getDateReservation() != null &&
                        r.getDateReservation().isBefore(expirationTime))
                .forEach(reservation -> {
                    reservation.setStatut(ReservationStatus.ANNULEE);
                    String commentaire = reservation.getCommentaire() != null ?
                            reservation.getCommentaire() + " | Réservation expirée le " + LocalDateTime.now() :
                            "Réservation expirée le " + LocalDateTime.now();
                    reservation.setCommentaire(commentaire);
                    reservationRepository.save(reservation);
                });
    }
}
