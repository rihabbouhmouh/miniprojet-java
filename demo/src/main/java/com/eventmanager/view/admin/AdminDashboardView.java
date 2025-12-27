package com.eventmanager.view.admin;

import com.eventmanager.entity.Event;
import com.eventmanager.enums.EventStatus;
import com.eventmanager.enums.ReservationStatus;
import com.eventmanager.enums.UserRole;
import com.eventmanager.repository.EventRepository;
import com.eventmanager.repository.ReservationRepository;
import com.eventmanager.repository.UserRepository;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.stream.Collectors;

@Route(value = "admin/dashboard", layout = MainLayout.class)
@PageTitle("Dashboard Admin - Event Manager")
@CssImport("./styles/admin-dashboard.css")
public class AdminDashboardView extends VerticalLayout {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;

    public AdminDashboardView(UserRepository userRepository,
                              EventRepository eventRepository,
                              ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("admin-dashboard");

        add(
                buildHeader(),
                buildStatsGrid(),
                buildQuickActions(),
                buildRecentActivity()
        );
    }

    private Component buildHeader() {
        Div header = new Div();
        header.addClassName("admin-header");

        H2 title = new H2("Admin Dashboard");
        title.addClassName("admin-title");

        Paragraph sub = new Paragraph("Overview of users, events, reservations and revenue.");
        sub.addClassName("admin-subtitle");

        header.add(title, sub);
        return header;
    }

    private Component buildStatsGrid() {
        Div grid = new Div();
        grid.addClassName("stats-grid");

        long totalUsers = userRepository.count();
        long adminCount = userRepository.findAll().stream().filter(u -> u.getRole() == UserRole.ADMIN).count();
        long organizerCount = userRepository.findAll().stream().filter(u -> u.getRole() == UserRole.ORGANIZER).count();
        long clientCount = userRepository.findAll().stream().filter(u -> u.getRole() == UserRole.CLIENT).count();

        long totalEvents = eventRepository.count();
        long publishedEvents = eventRepository.findAll().stream().filter(e -> e.getStatut() == EventStatus.PUBLIE).count();
        long draftEvents = eventRepository.findAll().stream().filter(e -> e.getStatut() == EventStatus.BROUILLON).count();

        long totalReservations = reservationRepository.count();
        long confirmedReservations = reservationRepository.findByStatut(ReservationStatus.CONFIRMEE).size();
        long pendingReservations = reservationRepository.findByStatut(ReservationStatus.EN_ATTENTE).size();

        double totalRevenue = reservationRepository.findAll().stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .mapToDouble(r -> r.getMontantTotal() != null ? r.getMontantTotal() : 0.0)
                .sum();

        grid.add(
                statCard("Users", String.valueOf(totalUsers),
                        "Admins: " + adminCount + " • Organizers: " + organizerCount + " • Clients: " + clientCount,
                        "accent-blue", VaadinIcon.USERS),

                statCard("Events", String.valueOf(totalEvents),
                        "Published: " + publishedEvents + " • Drafts: " + draftEvents,
                        "accent-purple", VaadinIcon.CALENDAR),

                statCard("Reservations", String.valueOf(totalReservations),
                        "Confirmed: " + confirmedReservations + " • Pending: " + pendingReservations,
                        "accent-pink", VaadinIcon.TICKET),

                statCard("Revenue", String.format("%.2f DH", totalRevenue),
                        "Total confirmed reservations",
                        "accent-cyan", VaadinIcon.MONEY)
        );

        return wrapSection("Key statistics", grid);
    }

    private Component buildQuickActions() {
        Div grid = new Div();
        grid.addClassName("actions-grid");

        grid.add(
                actionCard("Manage Users", "admin/users", VaadinIcon.USERS, "accent-blue"),
                actionCard("All Events", "admin/events", VaadinIcon.CALENDAR_CLOCK, "accent-purple"),
                actionCard("All Reservations", "admin/reservations", VaadinIcon.RECORDS, "accent-pink")
        );

        return wrapSection("Quick actions", grid);
    }

    private Component buildRecentActivity() {
        Div list = new Div();
        list.addClassName("activity-list");

        List<Event> recentEvents = eventRepository.findAll().stream()
                .filter(e -> e.getDateCreation() != null)
                .sorted((e1, e2) -> e2.getDateCreation().compareTo(e1.getDateCreation()))
                .limit(6)
                .collect(Collectors.toList());

        if (recentEvents.isEmpty()) {
            Div empty = new Div(new Text("No recent events."));
            empty.addClassName("empty-state");
            list.add(empty);
        } else {
            recentEvents.forEach(event -> {
                Div row = new Div();
                row.addClassName("activity-row");

                Span title = new Span(event.getTitre());
                title.addClassName("activity-title");

                Span status = new Span(event.getStatut().name());
                status.addClassName("status-pill");
                status.addClassName("status-" + event.getStatut().name().toLowerCase());

                Span meta = new Span(event.getCategorie() != null ? event.getCategorie().getLabel() : "—");
                meta.addClassName("activity-meta");

                row.add(title, meta, status);
                list.add(row);
            });
        }

        return wrapSection("Recent activity", list);
    }

    private Component wrapSection(String label, Component content) {
        Div section = new Div();
        section.addClassName("admin-section");

        Div head = new Div();
        head.addClassName("section-head");

        H3 title = new H3(label);
        title.addClassName("section-title");

        head.add(title);
        section.add(head, content);
        return section;
    }

    private Div statCard(String title, String value, String subtitle, String accentClass, VaadinIcon icon) {
        Div card = new Div();
        card.addClassName("stat-card");
        card.addClassName(accentClass);

        Div top = new Div();
        top.addClassName("stat-top");

        Div left = new Div();
        left.addClassName("stat-left");

        Span t = new Span(title);
        t.addClassName("stat-title");

        Span v = new Span(value);
        v.addClassName("stat-value");

        Span s = new Span(subtitle);
        s.addClassName("stat-subtitle");

        left.add(t, v, s);

        Icon ic = icon.create();
        ic.addClassName("stat-icon");

        top.add(left, ic);

        card.add(top);
        return card;
    }

    private Div actionCard(String title, String route, VaadinIcon icon, String accentClass) {
        Div card = new Div();
        card.addClassName("action-card");
        card.addClassName(accentClass);

        Icon ic = icon.create();
        ic.addClassName("action-icon");

        Span t = new Span(title);
        t.addClassName("action-title");

        Span hint = new Span("Open");
        hint.addClassName("action-hint");

        card.add(ic, t, hint);

        card.getElement().addEventListener("click", e -> getUI().ifPresent(ui -> ui.navigate(route)));
        return card;
    }
}
