package com.eventmanager.entity;

import com.eventmanager.enums.ReservationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservations",
        uniqueConstraints = @UniqueConstraint(columnNames = "code_reservation"))
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evenement_id", nullable = false)
    private Event evenement;

    @Min(value = 1, message = "Le nombre de places doit être supérieur à 0")
    @Column(name = "nombre_places", nullable = false)
    private Integer nombrePlaces;

    @Column(name = "montant_total", nullable = false)
    private Double montantTotal;

    @Column(name = "date_reservation", nullable = false)
    private LocalDateTime dateReservation = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus statut = ReservationStatus.EN_ATTENTE;

    @Column(name = "code_reservation", unique = true, nullable = false, updatable = false)
    private String codeReservation;

    private String commentaire;

    // Méthode unique qui combine toutes les validations
    @PrePersist
    @PreUpdate
    private void validateAndCalculate() {
        // 1. Génération automatique du code de réservation
        if (codeReservation == null) {
            String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            this.codeReservation = "EVT-" + uuid;
        }

        // 2. Calcul automatique du montant total
        if (evenement != null && nombrePlaces != null) {
            this.montantTotal = evenement.getPrixUnitaire() * nombrePlaces;
        }

        // 3. Validation du nombre de places vs capacité
        if (evenement != null && nombrePlaces != null) {
            // Calculer les places déjà réservées (sans compter les annulées)
            long placesReservees = evenement.getReservations().stream()
                    .filter(r -> r != this && r.getStatut() != ReservationStatus.ANNULEE)
                    .mapToLong(Reservation::getNombrePlaces)
                    .sum();

            long placesDisponibles = evenement.getCapaciteMax() - placesReservees;

            if (nombrePlaces > placesDisponibles) {
                throw new IllegalArgumentException(
                        String.format("Nombre de places insuffisant. Disponible: %d, Demandé: %d",
                                placesDisponibles, nombrePlaces)
                );
            }
        }
    }

    // Constructeurs
    public Reservation() {}

    public Reservation(User utilisateur, Event evenement, Integer nombrePlaces) {
        this.utilisateur = utilisateur;
        this.evenement = evenement;
        this.nombrePlaces = nombrePlaces;
        // Appel manuel de la validation
        this.validateAndCalculate();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUtilisateur() { return utilisateur; }
    public void setUtilisateur(User utilisateur) {
        this.utilisateur = utilisateur;
        this.validateAndCalculate();
    }

    public Event getEvenement() { return evenement; }
    public void setEvenement(Event evenement) {
        this.evenement = evenement;
        this.validateAndCalculate();
    }

    public Integer getNombrePlaces() { return nombrePlaces; }
    public void setNombrePlaces(Integer nombrePlaces) {
        this.nombrePlaces = nombrePlaces;
        this.validateAndCalculate();
    }

    public Double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(Double montantTotal) { this.montantTotal = montantTotal; }

    public LocalDateTime getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDateTime dateReservation) { this.dateReservation = dateReservation; }

    public ReservationStatus getStatut() { return statut; }
    public void setStatut(ReservationStatus statut) { this.statut = statut; }

    public String getCodeReservation() { return codeReservation; }
    public void setCodeReservation(String codeReservation) { this.codeReservation = codeReservation; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    // Méthode utilitaire pour afficher les infos de réservation
    public String getInfoReservation() {
        return String.format("Réservation %s - %s places pour %s",
                codeReservation, nombrePlaces, evenement != null ? evenement.getTitre() : "N/A");
    }

}