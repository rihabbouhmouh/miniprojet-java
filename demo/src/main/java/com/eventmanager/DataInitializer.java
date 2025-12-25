package com.eventmanager;

import com.eventmanager.entity.Event;
import com.eventmanager.entity.Reservation;
import com.eventmanager.entity.User;
import com.eventmanager.enums.EventCategory;
import com.eventmanager.enums.EventStatus;
import com.eventmanager.enums.ReservationStatus;
import com.eventmanager.enums.UserRole;
import com.eventmanager.repository.UserRepository;
import com.eventmanager.repository.EventRepository;
import com.eventmanager.repository.ReservationRepository;
import com.eventmanager.service.SecurityService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Initialise les données de base : utilisateurs (admin, client, organisateur) et événements
 * S'exécute au démarrage de l'application si data.sql n'a pas déjà créé les données
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final SecurityService securityService;

    public DataInitializer(UserRepository userRepository, EventRepository eventRepository, ReservationRepository reservationRepository,
                           SecurityService securityService) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.securityService = securityService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("=== Initialisation des données ===");

                // =====================
        // Création des réservations
        // =====================
        if (reservationRepository.count() == 0) {

            User client = userRepository.findByEmail("client@event.ma")
                    .orElseThrow(() -> new IllegalStateException("Client non trouvé"));

            List<Event> events = eventRepository.findAll();

            // Réservation 1
            Reservation r1 = new Reservation(client, events.get(0), 2);
            r1.setStatut(ReservationStatus.CONFIRMEE);
            reservationRepository.save(r1);

            // Réservation 2
            Reservation r2 = new Reservation(client, events.get(1), 1);
            r2.setStatut(ReservationStatus.EN_ATTENTE);
            reservationRepository.save(r2);

            // Réservation 3
            Reservation r3 = new Reservation(client, events.get(2), 3);
            r3.setStatut(ReservationStatus.ANNULEE);
            reservationRepository.save(r3);

            // Réservation 4
            Reservation r4 = new Reservation(client, events.get(3), 1);
            r4.setStatut(ReservationStatus.CONFIRMEE);
            reservationRepository.save(r4);

            System.out.println("✓ " + reservationRepository.count() + " réservations créées");
        }

        
        // Toujours créer/mettre à jour les utilisateurs de base pour garantir les mots de passe corrects
        // Cela assure que même si data.sql a créé les utilisateurs, les mots de passe sont corrects
        createUserIfNotExists("Admin", "System", "admin@event.ma", 
                "admin123", "0612345678", UserRole.ADMIN);
        createUserIfNotExists("Client", "Test", "client@event.ma", 
                "client123", "0623456789", UserRole.CLIENT);
        User organisateur = createUserIfNotExists("Organisateur", "Event", "organisateur@event.ma", 
                "org123", "0634567890", UserRole.ORGANIZER);

        System.out.println("✓ Utilisateurs vérifiés/mis à jour : Admin, Client, Organisateur");
        
        // Vérifier si des événements existent déjà
        if (eventRepository.count() > 0) {
            System.out.println("Des événements existent déjà. Création d'événements ignorée.");
            return;
        }

        // Créer des événements
        LocalDateTime now = LocalDateTime.now();
        
        // Événement 1 : Concert
        Event event1 = new Event();
        event1.setTitre("Concert de Musique Classique");
        event1.setDescription("Un magnifique concert de musique classique avec l'orchestre philharmonique. Au programme : Beethoven, Mozart et Chopin.");
        event1.setCategorie(EventCategory.CONCERT);
        event1.setDateDebut(now.plusDays(15));
        event1.setDateFin(now.plusDays(15).plusHours(3));
        event1.setLieu("Théâtre National");
        event1.setVille("Rabat");
        event1.setCapaciteMax(200);
        event1.setPrixUnitaire(150.0);
        event1.setImageUrl("https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f");
        event1.setOrganisateur(organisateur);
        event1.setStatut(EventStatus.PUBLIE);
        eventRepository.save(event1);

        // Événement 2 : Théâtre
        Event event2 = new Event();
        event2.setTitre("Pièce de Théâtre : Le Misanthrope");
        event2.setDescription("Mise en scène moderne de la célèbre pièce de Molière par la troupe nationale.");
        event2.setCategorie(EventCategory.THEATRE);
        event2.setDateDebut(now.plusDays(20));
        event2.setDateFin(now.plusDays(20).plusHours(2));
        event2.setLieu("Théâtre Mohammed V");
        event2.setVille("Casablanca");
        event2.setCapaciteMax(150);
        event2.setPrixUnitaire(120.0);
        event2.setOrganisateur(organisateur);
        event2.setStatut(EventStatus.PUBLIE);
        eventRepository.save(event2);

        // Événement 3 : Conférence
        Event event3 = new Event();
        event3.setTitre("Conférence sur l'Intelligence Artificielle");
        event3.setDescription("Conférence avec des experts internationaux sur les dernières avancées en IA et leur impact sur la société.");
        event3.setCategorie(EventCategory.CONFERENCE);
        event3.setDateDebut(now.plusDays(30));
        event3.setDateFin(now.plusDays(30).plusHours(4));
        event3.setLieu("Palais des Congrès");
        event3.setVille("Marrakech");
        event3.setCapaciteMax(300);
        event3.setPrixUnitaire(250.0);
        event3.setImageUrl("https://images.unsplash.com/photo-1485827404703-89b55fcc595e");
        event3.setOrganisateur(organisateur);
        event3.setStatut(EventStatus.PUBLIE);
        eventRepository.save(event3);

        // Événement 4 : Sport
        Event event4 = new Event();
        event4.setTitre("Marathon de la Ville");
        event4.setDescription("Marathon annuel de 42 km à travers la ville. Catégories : Elite, Amateurs, Fun Run (5 km).");
        event4.setCategorie(EventCategory.SPORT);
        event4.setDateDebut(now.plusDays(25));
        event4.setDateFin(now.plusDays(25).plusHours(6));
        event4.setLieu("Place de la Victoire");
        event4.setVille("Tanger");
        event4.setCapaciteMax(500);
        event4.setPrixUnitaire(80.0);
        event4.setImageUrl("https://images.unsplash.com/photo-1571008887538-b36bb32f4571");
        event4.setOrganisateur(organisateur);
        event4.setStatut(EventStatus.PUBLIE);
        eventRepository.save(event4);

        // Événement 5 : Autre
        Event event5 = new Event();
        event5.setTitre("Exposition d'Art Contemporain");
        event5.setDescription("Exposition présentant les œuvres d'artistes marocains émergents. Peinture, sculpture et photographie.");
        event5.setCategorie(EventCategory.AUTRE);
        event5.setDateDebut(now.plusDays(10));
        event5.setDateFin(now.plusDays(12));
        event5.setLieu("Galerie d'Art Moderne");
        event5.setVille("Fès");
        event5.setCapaciteMax(100);
        event5.setPrixUnitaire(50.0);
        event5.setOrganisateur(organisateur);
        event5.setStatut(EventStatus.PUBLIE);
        eventRepository.save(event5);

        // Événement 6 : En brouillon
        Event event6 = new Event();
        event6.setTitre("Festival de Jazz");
        event6.setDescription("Festival de jazz avec des artistes locaux et internationaux. Deux jours de musique non-stop.");
        event6.setCategorie(EventCategory.CONCERT);
        event6.setDateDebut(now.plusDays(45));
        event6.setDateFin(now.plusDays(46));
        event6.setLieu("Parc de la Ligue Arabe");
        event6.setVille("Casablanca");
        event6.setCapaciteMax(1000);
        event6.setPrixUnitaire(200.0);
        event6.setOrganisateur(organisateur);
        event6.setStatut(EventStatus.BROUILLON);
        eventRepository.save(event6);

        System.out.println("✓ " + eventRepository.count() + " événements créés");
        System.out.println("=== Initialisation terminée ===");
    }

    private User createUserIfNotExists(String nom, String prenom, String email, 
                                      String password, String telephone, UserRole role) {
        return userRepository.findByEmail(email)
                .map(existingUser -> {
                    // Si l'utilisateur existe, mettre à jour le mot de passe et s'assurer que tout est correct
                    // Cela garantit que le mot de passe correspond bien à celui attendu
                    existingUser.setPassword(securityService.hashPassword(password));
                    existingUser.setNom(nom);
                    existingUser.setPrenom(prenom);
                    existingUser.setRole(role);
                    existingUser.setTelephone(telephone);
                    existingUser.setActif(true);
                    userRepository.save(existingUser);
                    System.out.println("✓ Utilisateur mis à jour: " + email);
                    return existingUser;
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setNom(nom);
                    user.setPrenom(prenom);
                    user.setEmail(email);
                    user.setPassword(securityService.hashPassword(password));
                    user.setTelephone(telephone);
                    user.setRole(role);
                    user.setActif(true);
                    user.setDateInscription(LocalDateTime.now());
                    return userRepository.save(user);
                });
    }
}
