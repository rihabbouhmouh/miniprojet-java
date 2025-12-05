package com.eventmanager.repository;

import com.eventmanager.entity.Reservation;
import com.eventmanager.enums.ReservationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Réservations d'un utilisateur
    List<Reservation> findByUtilisateurId(Long utilisateurId);

    // Réservations d'un événement avec statut donné
    List<Reservation> findByEvenementIdAndStatut(Long evenementId, ReservationStatus statut);

    // Nombre total de places réservées pour un événement
    @Query("SELECT COALESCE(SUM(r.nombrePlaces), 0) FROM Reservation r " +
            "WHERE r.evenement.id = :evenementId AND r.statut != 'ANNULEE'")
    Long countReservedPlacesByEventId(@Param("evenementId") Long evenementId);

    // Trouver une réservation par code
    Optional<Reservation> findByCodeReservation(String codeReservation);

    // Réservations entre deux dates
    List<Reservation> findByDateReservationBetween(LocalDateTime start, LocalDateTime end);

    // Réservations confirmées d'un utilisateur
    List<Reservation> findByUtilisateurIdAndStatut(Long utilisateurId, ReservationStatus statut);

    // Montant total des réservations confirmées d'un utilisateur
    @Query("SELECT COALESCE(SUM(r.montantTotal), 0) FROM Reservation r " +
            "WHERE r.utilisateur.id = :utilisateurId AND r.statut = 'CONFIRMEE'")
    Double calculateTotalAmountByUser(@Param("utilisateurId") Long utilisateurId);

    // Réservations par statut
    List<Reservation> findByStatut(ReservationStatus statut);

    // Réservations d'un événement
    List<Reservation> findByEvenementId(Long evenementId);

    // Vérifier si un utilisateur a déjà réservé un événement
    boolean existsByUtilisateurIdAndEvenementId(Long utilisateurId, Long evenementId);

    // Statistiques journalières (PostgreSQL)
    @Query("SELECT CAST(r.dateReservation AS date), COUNT(r), SUM(r.montantTotal) " +
            "FROM Reservation r WHERE r.statut = 'CONFIRMEE' " +
            "AND r.dateReservation BETWEEN :start AND :end " +
            "GROUP BY CAST(r.dateReservation AS date) ORDER BY CAST(r.dateReservation AS date)")
    List<Object[]> getDailyReservationStats(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    // Réservations en attente depuis plus de X heures
    @Query("SELECT r FROM Reservation r WHERE r.statut = 'EN_ATTENTE' " +
            "AND r.dateReservation < :dateLimite")
    List<Reservation> findPendingReservationsOlderThan(@Param("dateLimite") LocalDateTime dateLimite);

    // Top clients (avec pagination)
    @Query("SELECT r.utilisateur, COUNT(r), SUM(r.montantTotal) " +
            "FROM Reservation r WHERE r.statut = 'CONFIRMEE' " +
            "GROUP BY r.utilisateur ORDER BY SUM(r.montantTotal) DESC")
    List<Object[]> findTopClients(Pageable pageable);

    // Statistiques de réservations par événement
    @Query("SELECT r.evenement, COUNT(r), SUM(r.nombrePlaces), SUM(r.montantTotal) " +
            "FROM Reservation r WHERE r.statut = 'CONFIRMEE' " +
            "GROUP BY r.evenement")
    List<Object[]> getReservationStatsByEvent();
}