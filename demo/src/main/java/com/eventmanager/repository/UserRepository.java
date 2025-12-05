package com.eventmanager.repository;

import com.eventmanager.entity.User;
import com.eventmanager.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(String nom, String prenom);

    @Query("SELECT u FROM User u WHERE LOWER(u.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByNomOrPrenom(@Param("keyword") String keyword);

    List<User> findByActifFalse();

    List<User> findByDateInscriptionAfter(LocalDateTime date);

    // CORRIGEZ cette méthode ou supprimez-la temporairement
    @Query("SELECT u FROM User u WHERE " +
            "(:nom IS NULL OR LOWER(u.nom) LIKE LOWER(CONCAT('%', :nom, '%'))) AND " +
            "(:prenom IS NULL OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :prenom, '%'))) AND " +
            "(:role IS NULL OR u.userRole = :role) AND " +  // CORRECTION : userRole au lieu de role
            "(:actif IS NULL OR u.actif = :actif)")
    List<User> searchUsers(@Param("nom") String nom,
                           @Param("prenom") String prenom,
                           @Param("role") UserRole role,
                           @Param("actif") Boolean actif);

    @Query("SELECT EXTRACT(MONTH FROM u.dateInscription) as mois, COUNT(u) as count " +
            "FROM User u " +
            "WHERE EXTRACT(YEAR FROM u.dateInscription) = :annee " +
            "GROUP BY EXTRACT(MONTH FROM u.dateInscription) " +
            "ORDER BY mois")
    List<Object[]> countUsersByMonth(@Param("annee") int annee);
}