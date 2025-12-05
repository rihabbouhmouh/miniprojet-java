package com.eventmanager.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void sendReservationConfirmation(String toEmail, String reservationCode,
                                            String eventTitle, String userName) {
        // Implémentation d'envoi d'email
        System.out.printf("Email envoyé à %s: Confirmation de réservation %s pour l'événement %s%n",
                toEmail, reservationCode, eventTitle);
    }

    public void sendEventCancellation(String toEmail, String eventTitle, String reason) {
        System.out.printf("Email envoyé à %s: Événement %s annulé: %s%n",
                toEmail, eventTitle, reason);
    }
}