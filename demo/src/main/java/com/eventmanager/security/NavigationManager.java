package com.eventmanager.security;

import com.eventmanager.entity.User;
import com.vaadin.flow.component.UI;
import org.springframework.stereotype.Component;

@Component
public class NavigationManager {

    private final AuthenticatedUser authenticatedUser;

    public NavigationManager(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    public void navigateToUserHome() {
        authenticatedUser.get().ifPresentOrElse(
                this::navigateBasedOnRole,
                () -> UI.getCurrent().navigate("login")
        );
    }

    private void navigateBasedOnRole(User user) {
        switch (user.getRole()) {
            case ADMIN -> UI.getCurrent().navigate("admin/dashboard");
            case ORGANIZER -> UI.getCurrent().navigate("organizer/dashboard");
            case CLIENT -> UI.getCurrent().navigate("dashboard");
            default -> UI.getCurrent().navigate("home");
        }
    }

    public void navigateTo(String route) {
        UI.getCurrent().navigate(route);
    }

    public void navigateToLogin() {
        UI.getCurrent().navigate("login");
    }

    public void navigateToHome() {
        UI.getCurrent().navigate("home");
    }

    public void navigateToEvents() {
        UI.getCurrent().navigate("events");
    }

    public void navigateToEventDetail(Long eventId) {
        UI.getCurrent().navigate("event/" + eventId);
    }

    public void navigateToReservation(Long eventId) {
        UI.getCurrent().navigate("event/" + eventId + "/reserve");
    }

    public void navigateToProfile() {
        UI.getCurrent().navigate("profile");
    }

    public void navigateToMyReservations() {
        UI.getCurrent().navigate("my-reservations");
    }

    public void navigateToOrganizerDashboard() {
        UI.getCurrent().navigate("organizer/dashboard");
    }

    public void navigateToMyEvents() {
        UI.getCurrent().navigate("organizer/events");
    }

    public void navigateToCreateEvent() {
        UI.getCurrent().navigate("organizer/event/new");
    }

    public void navigateToEditEvent(Long eventId) {
        UI.getCurrent().navigate("organizer/event/edit/" + eventId);
    }

    public void navigateToEventReservations(Long eventId) {
        UI.getCurrent().navigate("organizer/event/" + eventId + "/reservations");
    }

    public void navigateToAdminDashboard() {
        UI.getCurrent().navigate("admin/dashboard");
    }

    public void goBack() {
        UI.getCurrent().getPage().getHistory().back();
    }
}