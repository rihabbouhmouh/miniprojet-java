package com.eventmanager.service;

import com.eventmanager.entity.Reservation;
import com.eventmanager.enums.ReservationStatus;
import java.util.List;
import java.util.Map;

public interface IReservationService {

    Reservation createReservation(Long userId, Long eventId, int nombrePlaces);

    Reservation updateReservationStatus(Long reservationId, ReservationStatus status);

    Reservation getReservationById(Long reservationId);

    List<Reservation> getReservationsByUser(Long userId);

    List<Reservation> getReservationsByEvent(Long eventId);

    List<Reservation> getReservationsByStatus(ReservationStatus status);

    void cancelReservation(Long reservationId);

    Reservation confirmReservation(Long reservationId);

    boolean checkEventAvailability(Long eventId, int requestedSeats);

    int getAvailableSeats(Long eventId);

    double calculateReservationAmount(Long reservationId);

    List<Reservation> getUserReservationsWithDetails(Long userId);

    Map<String, Object> getReservationStatistics(Long eventId);

    List<Reservation> searchReservations(String keyword);

    Reservation updateReservation(Long reservationId, int newNombrePlaces);
}