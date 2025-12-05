package com.eventmanager.entity;

import com.eventmanager.enums.EventCategory;
import com.eventmanager.enums.EventStatus;
import com.eventmanager.enums.ReservationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 100, message = "Le titre doit contenir entre 5 et 100 caractères")
    @Column(nullable = false)
    private String titre;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventCategory categorie = EventCategory.AUTRE;

    @Future(message = "La date de début doit être dans le futur")
    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDateTime dateFin;

    @NotBlank(message = "Le lieu est obligatoire")
    @Column(nullable = false)
    private String lieu;

    @NotBlank(message = "La ville est obligatoire")
    @Column(nullable = false)
    private String ville;

    @Min(value = 1, message = "La capacité maximale doit être supérieure à 0")
    @Column(name = "capacite_max", nullable = false)
    private Integer capaciteMax;

    @DecimalMin(value = "0.0", message = "Le prix ne peut pas être négatif")
    @Column(name = "prix_unitaire", nullable = false)
    private Double prixUnitaire;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisateur_id", nullable = false)
    private User organisateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus statut = EventStatus.BROUILLON;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    // Validation et mise à jour combinée
    @PrePersist
    @PreUpdate
    private void validateAndUpdate() {
        // Validation des dates
        if (dateFin != null && dateDebut != null && !dateFin.isAfter(dateDebut)) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }

        // Mise à jour de la date de modification
        this.dateModification = LocalDateTime.now();
    }

    // Constructeurs
    public Event() {}

    public Event(String titre, String description, LocalDateTime dateDebut, LocalDateTime dateFin,
                 String lieu, String ville, Integer capaciteMax, Double prixUnitaire, User organisateur) {
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lieu = lieu;
        this.ville = ville;
        this.capaciteMax = capaciteMax;
        this.prixUnitaire = prixUnitaire;
        this.organisateur = organisateur;
        // Appel manuel de la validation pour le constructeur
        validateDates();
    }

    // Méthode privée pour valider les dates (utilisée dans le constructeur)
    private void validateDates() {
        if (dateFin != null && dateDebut != null && !dateFin.isAfter(dateDebut)) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public EventCategory getCategorie() { return categorie; }
    public void setCategorie(EventCategory categorie) { this.categorie = categorie; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
        if (this.dateFin != null) {
            validateDates();
        }
    }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
        if (this.dateDebut != null) {
            validateDates();
        }
    }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public Integer getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(Integer capaciteMax) { this.capaciteMax = capaciteMax; }

    public Double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(Double prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public User getOrganisateur() { return organisateur; }
    public void setOrganisateur(User organisateur) { this.organisateur = organisateur; }

    public EventStatus getStatut() { return statut; }
    public void setStatut(EventStatus statut) {
        this.statut = statut;
        this.dateModification = LocalDateTime.now();
    }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    public List<Reservation> getReservations() { return reservations; }
    public void setReservations(List<Reservation> reservations) { this.reservations = reservations; }

    // Méthode utilitaire pour calculer les places disponibles
    public int getPlacesDisponibles() {
        if (reservations == null || reservations.isEmpty()) {
            return capaciteMax;
        }

        long placesReservees = reservations.stream()
                .filter(r -> r.getStatut() != null && r.getStatut() != ReservationStatus.ANNULEE)
                .mapToLong(Reservation::getNombrePlaces)
                .sum();

        return (int) (capaciteMax - placesReservees);
    }

    // Méthode pour vérifier si l'événement est complet
    public boolean isComplet() {
        return getPlacesDisponibles() <= 0;
    }

    // Méthode pour vérifier si l'événement est en cours
    public boolean isEnCours() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(dateDebut) && now.isBefore(dateFin);
    }

    // Méthode pour vérifier si l'événement est passé
    public boolean isPasse() {
        return LocalDateTime.now().isAfter(dateFin);
    }

    // Méthode pour vérifier si l'événement est à venir
    public boolean isAVenir() {
        return LocalDateTime.now().isBefore(dateDebut);
    }
}