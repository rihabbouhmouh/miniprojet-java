package com.eventmanager.dto;

import com.eventmanager.enums.UserRole;

public class RegisterRequest {

    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String telephone;
    private UserRole userRole;

    public RegisterRequest() {}

    public RegisterRequest(String nom, String prenom, String email, String password, String telephone, UserRole userRole) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.telephone = telephone;
        this.userRole = userRole;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public UserRole getRole() { return userRole; }
    public void setRole(UserRole userRole) { this.userRole = userRole; }
}
