package com.eventmanager.service;

import com.eventmanager.entity.Event;
import com.eventmanager.enums.EventCategory;
import com.eventmanager.enums.EventStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface IEventService {

    Event createEvent(Event event, Long organizerId);

    Event updateEvent(Long eventId, Event eventDetails);

    Event getEventById(Long eventId);

    List<Event> getAllEvents();

    List<Event> getUpcomingEvents();

    List<Event> getEventsByOrganizer(Long organizerId);

    List<Event> getEventsByCategory(String category);

    List<Event> getEventsByStatus(EventStatus status);

    List<Event> searchEvents(String keyword, String location,
                             LocalDateTime startDate, LocalDateTime endDate);

    void deleteEvent(Long eventId);

    Event changeEventStatus(Long eventId, EventStatus newStatus);

    boolean isEventFull(Long eventId);

    int getAvailableSeats(Event event); // 🔹 accept Event directement

    Map<String, Object> getEventStatistics(Long eventId);

    List<Event> getFeaturedEvents(int limit);

    List<Event> getEventsWithFilters(String title, String location,
                                     String category, Double minPrice, Double maxPrice);

    Event updateEventSeats(Long eventId, int newTotalSeats);

    boolean isUserEventOrganizer(Long eventId, Long userId);

    List<Event> getAvailableEvents();

    List<Event> getPopularEvents(int limit);

    List<Event> searchEventsByFilters(String keyword, String ville,
                                      EventCategory categorie, Double prixMin, Double prixMax);
}
