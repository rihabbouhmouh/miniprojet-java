package com.eventmanager.dto;

import com.eventmanager.enums.EventCategory;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class EventFormDTO {

    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 100, message = "Le titre doit contenir entre 5 et 100 caractères")
    private String titre;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @NotNull(message = "La catégorie est obligatoire")
    private EventCategory categorie;

    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    private LocalDateTime dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime dateFin;

    @NotBlank(message = "Le lieu est obligatoire")
    private String lieu;

    @NotBlank(message = "La ville est obligatoire")
    private String ville;

    @NotNull(message = "La capacité maximale est obligatoire")
    @Min(value = 1, message = "La capacité maximale doit être supérieure à 0")
    private Integer capaciteMax;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.0", message = "Le prix ne peut pas être négatif")
    private Double prixUnitaire;

    private String imageUrl;

    private Long organisateurId;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public EventCategory getCategorie() { return categorie; }
    public void setCategorie(EventCategory categorie) { this.categorie = categorie; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

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

    public Long getOrganisateurId() { return organisateurId; }
    public void setOrganisateurId(Long organisateurId) { this.organisateurId = organisateurId; }
}