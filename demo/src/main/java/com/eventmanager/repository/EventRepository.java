package com.eventmanager.repository;

import com.eventmanager.entity.Event;
import com.eventmanager.enums.EventCategory;
import com.eventmanager.enums.EventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Trouver les événements par catégorie
    List<Event> findByCategorie(EventCategory categorie);

    // Trouver les événements publiés entre deux dates de création
    List<Event> findByDateCreationBetween(LocalDateTime start, LocalDateTime end);

    // Trouver les événements d'un organisateur avec un statut donné
    List<Event> findByOrganisateurIdAndStatut(Long organisateurId, EventStatus statut);

    // ✅ Correction : chargement des réservations avec JOIN FETCH pour éviter LazyInitializationException
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.reservations r WHERE e.statut = 'PUBLIE' AND e.dateFin > CURRENT_TIMESTAMP")
    List<Event> findAvailableEventsWithReservations();

    // Compter le nombre d'événements par catégorie
    @Query("SELECT e.categorie, COUNT(e) FROM Event e GROUP BY e.categorie")
    List<Object[]> countByCategorie();

    // Trouver les événements par lieu ou ville
    List<Event> findByLieuContainingIgnoreCaseOrVilleContainingIgnoreCase(String lieu, String ville);

    // Rechercher les événements par titre (mot-clé)
    List<Event> findByTitreContainingIgnoreCase(String keyword);

    // Trouver les événements par plage de prix
    List<Event> findByPrixUnitaireBetween(Double prixMin, Double prixMax);

    // Recherche avancée d'événements avec filtres
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.reservations r WHERE " +
            "(:keyword IS NULL OR LOWER(e.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:ville IS NULL OR LOWER(e.ville) LIKE LOWER(CONCAT('%', :ville, '%'))) AND " +
            "(:categorie IS NULL OR e.categorie = :categorie) AND " +
            "(:prixMin IS NULL OR e.prixUnitaire >= :prixMin) AND " +
            "(:prixMax IS NULL OR e.prixUnitaire <= :prixMax) AND " +
            "e.statut = 'PUBLIE' AND e.dateFin > CURRENT_TIMESTAMP")
    List<Event> searchEvents(@Param("keyword") String keyword,
                             @Param("ville") String ville,
                             @Param("categorie") EventCategory categorie,
                             @Param("prixMin") Double prixMin,
                             @Param("prixMax") Double prixMax);

    // Événements populaires
    @Query("SELECT e, COUNT(r) as reservationCount FROM Event e LEFT JOIN e.reservations r " +
            "WHERE r.statut != 'ANNULEE' GROUP BY e ORDER BY reservationCount DESC")
    List<Object[]> findPopularEvents(Pageable pageable);

    // Événements à venir avant une date donnée
    @Query("SELECT e FROM Event e WHERE e.dateDebut BETWEEN CURRENT_TIMESTAMP AND :dateLimit AND e.statut = 'PUBLIE'")
    List<Event> findUpcomingEvents(@Param("dateLimit") LocalDateTime dateLimit);

    // ✅ Nombre total de places réservées pour un événement
    @Query("SELECT COALESCE(SUM(r.nombrePlaces), 0) FROM Reservation r WHERE r.evenement.id = :eventId AND r.statut != 'ANNULEE'")
    int countReservedSeats(@Param("eventId") Long eventId);

    // Statistiques de revenus
    @Query("SELECT e, COALESCE(SUM(r.montantTotal), 0) as revenue FROM Event e LEFT JOIN e.reservations r " +
            "WHERE r.statut = 'CONFIRMEE' GROUP BY e ORDER BY revenue DESC")
    List<Object[]> getEventRevenue();
}
