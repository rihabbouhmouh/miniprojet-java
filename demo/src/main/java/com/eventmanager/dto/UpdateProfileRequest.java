package com.eventmanager.dto;

public class UpdateProfileRequest {

    private Long userId;
    private String nom;
    private String prenom;
    private String telephone;

    public UpdateProfileRequest() {}

    public UpdateProfileRequest(Long userId, String nom, String prenom, String telephone) {
        this.userId = userId;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
}
