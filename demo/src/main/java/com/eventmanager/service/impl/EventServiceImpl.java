package com.eventmanager.service.impl;

import com.eventmanager.entity.Event;
import com.eventmanager.entity.User;
import com.eventmanager.enums.EventCategory;
import com.eventmanager.enums.EventStatus;
import com.eventmanager.enums.ReservationStatus;
import com.eventmanager.exception.BusinessException;
import com.eventmanager.exception.ResourceNotFoundException;
import com.eventmanager.repository.EventRepository;
import com.eventmanager.repository.ReservationRepository;
import com.eventmanager.service.IEventService;
import com.eventmanager.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventServiceImpl implements IEventService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final IUserService userService;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository,
                            ReservationRepository reservationRepository,
                            IUserService userService) {
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.userService = userService;
    }

    @Override
    public Event createEvent(Event event, Long organizerId) {
        User organizer = userService.getUserById(organizerId);

        if (!userService.isOrganizerOrAdmin(organizer)) {
            throw new BusinessException("Seuls les organisateurs ou administrateurs peuvent créer des événements");
        }

        LocalDateTime now = LocalDateTime.now();
        if (event.getDateDebut() == null || event.getDateFin() == null) {
            throw new BusinessException("Les dates sont obligatoires");
        }

        if (event.getDateDebut().isBefore(now)) {
            throw new BusinessException("La date de début doit être dans le futur");
        }

        if (!event.getDateFin().isAfter(event.getDateDebut())) {
            throw new BusinessException("La date de fin doit être après la date de début");
        }

        if (event.getCapaciteMax() == null || event.getCapaciteMax() <= 0) {
            throw new BusinessException("La capacité maximale doit être positive");
        }

        if (event.getPrixUnitaire() == null || event.getPrixUnitaire() < 0) {
            throw new BusinessException("Le prix unitaire ne peut pas être négatif");
        }

        event.setOrganisateur(organizer);
        event.setCategorie(event.getCategorie() != null ? event.getCategorie() : EventCategory.AUTRE);
        event.setStatut(EventStatus.BROUILLON);

        return eventRepository.save(event);
    }

    @Override
    public Event updateEvent(Long eventId, Event eventDetails) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement non trouvé"));

        if (event.isPasse()) {
            throw new BusinessException("Impossible de modifier un événement terminé");
        }

        if (eventDetails.getTitre() != null) event.setTitre(eventDetails.getTitre());
        if (eventDetails.getDescription() != null) event.setDescription(eventDetails.getDescription());
        if (eventDetails.getCategorie() != null) event.setCategorie(eventDetails.getCategorie());
        if (eventDetails.getDateDebut() != null) event.setDateDebut(eventDetails.getDateDebut());
        if (eventDetails.getDateFin() != null) event.setDateFin(eventDetails.getDateFin());
        if (eventDetails.getLieu() != null) event.setLieu(eventDetails.getLieu());
        if (eventDetails.getVille() != null) event.setVille(eventDetails.getVille());
        if (eventDetails.getCapaciteMax() != null) event.setCapaciteMax(eventDetails.getCapaciteMax());
        if (eventDetails.getPrixUnitaire() != null) event.setPrixUnitaire(eventDetails.getPrixUnitaire());
        if (eventDetails.getImageUrl() != null) event.setImageUrl(eventDetails.getImageUrl());

        return eventRepository.save(event);
    }

    @Override
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement non trouvé"));
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public List<Event> getUpcomingEvents() {
        return eventRepository.findUpcomingEvents(LocalDateTime.now().plusMonths(1));
    }

    @Override
    public List<Event> getEventsByOrganizer(Long organizerId) {
        return eventRepository.findByOrganisateurIdAndStatut(organizerId, null);
    }

    @Override
    public List<Event> getEventsByCategory(String category) {
        try {
            EventCategory cat = EventCategory.valueOf(category.toUpperCase());
            return eventRepository.findByCategorie(cat);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    @Override
    public List<Event> getEventsByStatus(EventStatus status) {
        return eventRepository.findAll().stream()
                .filter(event -> event.getStatut() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> searchEvents(String keyword, String location,
                                    LocalDateTime startDate, LocalDateTime endDate) {
        if (keyword != null && !keyword.isEmpty()) {
            return eventRepository.findByTitreContainingIgnoreCase(keyword);
        }
        return eventRepository.findAll();
    }

    @Override
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement non trouvé"));

        boolean hasReservations = reservationRepository.findAll()
                .stream()
                .anyMatch(r -> r.getEvenement() != null &&
                        r.getEvenement().getId().equals(eventId));

        if (hasReservations) {
            throw new BusinessException("Impossible de supprimer un événement avec des réservations");
        }

        eventRepository.delete(event);
    }

    @Override
    public Event changeEventStatus(Long eventId, EventStatus newStatus) {
        Event event = getEventById(eventId);
        event.setStatut(newStatus);
        return eventRepository.save(event);
    }

    @Override
    public boolean isEventFull(Long eventId) {
        Event event = getEventById(eventId);
        long reservedPlaces = reservationRepository.findAll().stream()
                .filter(r -> r.getEvenement() != null &&
                        r.getEvenement().getId().equals(eventId) &&
                        (r.getStatut() == ReservationStatus.CONFIRMEE ||
                                r.getStatut() == ReservationStatus.EN_ATTENTE))
                .mapToLong(r -> r.getNombrePlaces() != null ? r.getNombrePlaces() : 0)
                .sum();
        return reservedPlaces >= event.getCapaciteMax();
    }

    @Override
    public int getAvailableSeats(Event event) {
        if (event == null) throw new BusinessException("Événement invalide");
        Long eventId = event.getId();

        long reservedPlaces = reservationRepository.findAll().stream()
                .filter(r -> r.getEvenement() != null &&
                        r.getEvenement().getId().equals(eventId) &&
                        (r.getStatut() == ReservationStatus.CONFIRMEE ||
                                r.getStatut() == ReservationStatus.EN_ATTENTE))
                .mapToLong(r -> r.getNombrePlaces() != null ? r.getNombrePlaces() : 0)
                .sum();

        return (int) Math.max(0, event.getCapaciteMax() - reservedPlaces);
    }

    @Override
    public Map<String, Object> getEventStatistics(Long eventId) {
        Event event = getEventById(eventId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("event", event);

        List<com.eventmanager.entity.Reservation> eventReservations = reservationRepository.findAll()
                .stream()
                .filter(r -> r.getEvenement() != null && r.getEvenement().getId().equals(eventId))
                .collect(Collectors.toList());

        stats.put("totalReservations", eventReservations.size());

        Map<String, Long> reservationsByStatus = eventReservations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getStatut() != null ? r.getStatut().toString() : "INCONNU",
                        Collectors.counting()
                ));
        stats.put("reservationsByStatus", reservationsByStatus);

        long totalPlacesReserved = eventReservations.stream()
                .mapToLong(r -> r.getNombrePlaces() != null ? r.getNombrePlaces() : 0)
                .sum();
        stats.put("totalPlacesReserved", totalPlacesReserved);

        double totalAmount = eventReservations.stream()
                .mapToDouble(r -> r.getMontantTotal() != null ? r.getMontantTotal() : 0.0)
                .sum();
        stats.put("totalAmount", totalAmount);

        double fillRate = event.getCapaciteMax() > 0 ?
                (double) totalPlacesReserved / event.getCapaciteMax() * 100 : 0;
        stats.put("fillRate", Math.round(fillRate * 100.0) / 100.0);

        return stats;
    }

    @Override
    public List<Event> getFeaturedEvents(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<Object[]> results = eventRepository.findPopularEvents(pageable);

            return results.stream()
                    .map(result -> (Event) result[0])
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return getUpcomingEvents().stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<Event> getEventsWithFilters(String title, String location,
                                            String category, Double minPrice, Double maxPrice) {
        try {
            EventCategory cat = category != null ? EventCategory.valueOf(category.toUpperCase()) : null;
            return eventRepository.searchEvents(title, location, cat, minPrice, maxPrice);
        } catch (Exception e) {
            List<Event> allEvents = eventRepository.findAll();

            return allEvents.stream()
                    .filter(event -> title == null || (event.getTitre() != null &&
                            event.getTitre().toLowerCase().contains(title.toLowerCase())))
                    .filter(event -> location == null ||
                            ((event.getLieu() != null && event.getLieu().toLowerCase().contains(location.toLowerCase())) ||
                                    (event.getVille() != null && event.getVille().toLowerCase().contains(location.toLowerCase()))))
                    .filter(event -> category == null ||
                            (event.getCategorie() != null && event.getCategorie().toString().equalsIgnoreCase(category)))
                    .filter(event -> minPrice == null || (event.getPrixUnitaire() != null && event.getPrixUnitaire() >= minPrice))
                    .filter(event -> maxPrice == null || (event.getPrixUnitaire() != null && event.getPrixUnitaire() <= maxPrice))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public Event updateEventSeats(Long eventId, int newTotalSeats) {
        Event event = getEventById(eventId);
        if (newTotalSeats <= 0) {
            throw new BusinessException("La capacité doit être positive");
        }

        long reservedPlaces = reservationRepository.findAll().stream()
                .filter(r -> r.getEvenement() != null &&
                        r.getEvenement().getId().equals(eventId) &&
                        (r.getStatut() == ReservationStatus.CONFIRMEE ||
                                r.getStatut() == ReservationStatus.EN_ATTENTE))
                .mapToLong(r -> r.getNombrePlaces() != null ? r.getNombrePlaces() : 0)
                .sum();

        if (newTotalSeats < reservedPlaces) {
            throw new BusinessException("La nouvelle capacité ne peut pas être inférieure au nombre de places déjà réservées");
        }

        event.setCapaciteMax(newTotalSeats);
        return eventRepository.save(event);
    }

    @Override
    public boolean isUserEventOrganizer(Long eventId, Long userId) {
        Event event = getEventById(eventId);
        return event.getOrganisateur() != null &&
                event.getOrganisateur().getId().equals(userId);
    }

    @Override
    public List<Event> getAvailableEvents() {
        return eventRepository.findAll().stream()
                .filter(event -> event.getStatut() == EventStatus.PUBLIE)
                .filter(event -> event.getDateFin() != null &&
                        event.getDateFin().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> getPopularEvents(int limit) {
        return getFeaturedEvents(limit);
    }

    @Override
    public List<Event> searchEventsByFilters(String keyword, String ville,
                                             EventCategory categorie, Double prixMin, Double prixMax) {
        return eventRepository.searchEvents(keyword, ville, categorie, prixMin, prixMax);
    }
}
