package com.eventmanager.dto;

import com.eventmanager.enums.ReservationStatus;
import java.time.LocalDateTime;

public class ReservationDTO {

    private Long id;
    private String codeReservation;
    private Long utilisateurId;
    private Long evenementId;
    private Integer nombrePlaces;
    private Double montantTotal;
    private ReservationStatus statut;
    private String commentaire;
    private LocalDateTime dateReservation;

    // Constructeurs
    public ReservationDTO() {}

    public ReservationDTO(Long id, String codeReservation, Long utilisateurId, Long evenementId,
                          Integer nombrePlaces, Double montantTotal, ReservationStatus statut,
                          String commentaire, LocalDateTime dateReservation) {
        this.id = id;
        this.codeReservation = codeReservation;
        this.utilisateurId = utilisateurId;
        this.evenementId = evenementId;
        this.nombrePlaces = nombrePlaces;
        this.montantTotal = montantTotal;
        this.statut = statut;
        this.commentaire = commentaire;
        this.dateReservation = dateReservation;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodeReservation() { return codeReservation; }
    public void setCodeReservation(String codeReservation) { this.codeReservation = codeReservation; }

    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }

    public Long getEvenementId() { return evenementId; }
    public void setEvenementId(Long evenementId) { this.evenementId = evenementId; }

    public Integer getNombrePlaces() { return nombrePlaces; }
    public void setNombrePlaces(Integer nombrePlaces) { this.nombrePlaces = nombrePlaces; }

    public Double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(Double montantTotal) { this.montantTotal = montantTotal; }

    public ReservationStatus getStatut() { return statut; }
    public void setStatut(ReservationStatus statut) { this.statut = statut; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDateTime getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDateTime dateReservation) { this.dateReservation = dateReservation; }
}
