package com.eventmanager;

import com.eventmanager.repository.UserRepository;
import com.eventmanager.repository.EventRepository;
import com.eventmanager.repository.ReservationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * DÉSACTIVÉ : On utilise data.sql à la place
 * Pour réactiver : décommenter @Component
 */
// @Component  // ← Commenté pour désactiver
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;

    public DataInitializer(UserRepository userRepository, EventRepository eventRepository,
                           ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("=== DataInitializer désactivé - Utilisation de data.sql ===");
    }
}