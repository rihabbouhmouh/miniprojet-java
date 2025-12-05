package com.eventmanager.dto;

import jakarta.validation.constraints.*;

public class ReservationRequestDTO {

    @NotNull(message = "L'utilisateur est obligatoire")
    private Long utilisateurId;

    @NotNull(message = "L'événement est obligatoire")
    private Long evenementId;

    @NotNull(message = "Le nombre de places est obligatoire")
    @Min(value = 1, message = "Le nombre de places doit être au moins 1")
    @Max(value = 10, message = "Le nombre de places ne peut pas dépasser 10")
    private Integer nombrePlaces;

    @Size(max = 500, message = "Le commentaire ne peut pas dépasser 500 caractères")
    private String commentaire;

    // Getters & Setters
    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }

    public Long getEvenementId() { return evenementId; }
    public void setEvenementId(Long evenementId) { this.evenementId = evenementId; }

    public Integer getNombrePlaces() { return nombrePlaces; }
    public void setNombrePlaces(Integer nombrePlaces) { this.nombrePlaces = nombrePlaces; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}