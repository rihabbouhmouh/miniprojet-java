package com.eventmanager.dto;

import com.eventmanager.enums.EventCategory;
import com.eventmanager.enums.EventStatus;
import java.time.LocalDateTime;

public class EventDTO {

    private Long id;
    private String titre;
    private String description;
    private String lieu;
    private String ville;
    private EventCategory categorie;
    private EventStatus statut;
    private Double prixUnitaire;
    private Integer capaciteMax;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private LocalDateTime dateCreation;
    private Long organisateurId;

    // Constructeurs
    public EventDTO() {}

    public EventDTO(Long id, String titre, String description, String lieu, String ville, EventCategory categorie,
                    EventStatus statut, Double prixUnitaire, Integer capaciteMax, LocalDateTime dateDebut,
                    LocalDateTime dateFin, LocalDateTime dateCreation, Long organisateurId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.lieu = lieu;
        this.ville = ville;
        this.categorie = categorie;
        this.statut = statut;
        this.prixUnitaire = prixUnitaire;
        this.capaciteMax = capaciteMax;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.dateCreation = dateCreation;
        this.organisateurId = organisateurId;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public EventCategory getCategorie() { return categorie; }
    public void setCategorie(EventCategory categorie) { this.categorie = categorie; }

    public EventStatus getStatut() { return statut; }
    public void setStatut(EventStatus statut) { this.statut = statut; }

    public Double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(Double prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public Integer getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(Integer capaciteMax) { this.capaciteMax = capaciteMax; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Long getOrganisateurId() { return organisateurId; }
    public void setOrganisateurId(Long organisateurId) { this.organisateurId = organisateurId; }
}
