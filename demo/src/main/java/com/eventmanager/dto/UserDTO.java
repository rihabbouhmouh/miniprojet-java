package com.eventmanager.dto;

import com.eventmanager.enums.UserRole;
import java.time.LocalDateTime;

public class UserDTO {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private UserRole userRole;
    private Boolean actif;
    private LocalDateTime dateInscription;

    // Constructeurs
    public UserDTO() {}

    public UserDTO(Long id, String nom, String prenom, String email, String telephone, UserRole userRole, Boolean actif, LocalDateTime dateInscription) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.userRole = userRole;
        this.actif = actif;
        this.dateInscription = dateInscription;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public UserRole getRole() { return userRole; }
    public void setRole(UserRole userRole) { this.userRole = userRole; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }
}
