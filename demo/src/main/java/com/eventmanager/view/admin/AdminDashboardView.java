package com.eventmanager.view.admin;

import com.eventmanager.entity.Event;
import com.eventmanager.enums.EventStatus;
import com.eventmanager.enums.ReservationStatus;
import com.eventmanager.enums.UserRole;
import com.eventmanager.repository.EventRepository;
import com.eventmanager.repository.ReservationRepository;
import com.eventmanager.repository.UserRepository;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;
import java.util.stream.Collectors;

@Route(value = "admin/dashboard", layout = MainLayout.class)
@PageTitle("Dashboard Admin - Event Manager")
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
        setPadding(true);
        setSpacing(true);
        addClassName("admin-dashboard");

        // Header
        H2 title = new H2("📊 Tableau de Bord Administrateur");
        title.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.Margin.Bottom.LARGE);
        add(title);

        // Statistics Cards
        add(createStatisticsSection());

        // Quick Actions
        add(createQuickActionsSection());

        // Recent Activity
        add(createRecentActivitySection());
    }

    private HorizontalLayout createStatisticsSection() {
        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setSpacing(true);
        statsLayout.getStyle().set("flex-wrap", "wrap");

        // Total Users
        long totalUsers = userRepository.count();
        long adminCount = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ADMIN)
                .count();
        long organizerCount = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ORGANIZER)
                .count();
        long clientCount = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.CLIENT)
                .count();

        statsLayout.add(createStatCard("👥 Utilisateurs", String.valueOf(totalUsers),
                "Admins: " + adminCount + " | Organisateurs: " + organizerCount + " | Clients: " + clientCount,
                "#667eea"));

        // Total Events
        long totalEvents = eventRepository.count();
        long publishedEvents = eventRepository.findAll().stream()
                .filter(e -> e.getStatut() == EventStatus.PUBLIE)
                .count();
        long draftEvents = eventRepository.findAll().stream()
                .filter(e -> e.getStatut() == EventStatus.BROUILLON)
                .count();

        statsLayout.add(createStatCard("🎭 Événements", String.valueOf(totalEvents),
                "Publiés: " + publishedEvents + " | Brouillons: " + draftEvents,
                "#764ba2"));

        // Total Reservations
        long totalReservations = reservationRepository.count();
        long confirmedReservations = reservationRepository.findByStatut(ReservationStatus.CONFIRMEE).size();
        long pendingReservations = reservationRepository.findByStatut(ReservationStatus.EN_ATTENTE).size();

        statsLayout.add(createStatCard("🎫 Réservations", String.valueOf(totalReservations),
                "Confirmées: " + confirmedReservations + " | En attente: " + pendingReservations,
                "#f093fb"));

        // Total Revenue
        double totalRevenue = reservationRepository.findAll().stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .mapToDouble(r -> r.getMontantTotal() != null ? r.getMontantTotal() : 0.0)
                .sum();

        statsLayout.add(createStatCard("💰 Revenus", String.format("%.2f DH", totalRevenue),
                "Total des réservations confirmées",
                "#4facfe"));

        return statsLayout;
    }

    private VerticalLayout createStatCard(String title, String value, String subtitle, String color) {
        VerticalLayout card = new VerticalLayout();
        card.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Padding.LARGE,
                LumoUtility.BoxShadow.SMALL
        );
        card.getStyle()
                .set("min-width", "250px")
                .set("flex", "1")
                .set("border-left", "4px solid " + color);

        H3 titleH3 = new H3(title);
        titleH3.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.FontWeight.NORMAL, LumoUtility.Margin.NONE);
        titleH3.getStyle().set("color", "#666");

        Span valueSpan = new Span(value);
        valueSpan.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.FontWeight.BOLD, LumoUtility.Margin.Vertical.SMALL);
        valueSpan.getStyle().set("color", color);

        Paragraph subtitleP = new Paragraph(subtitle);
        subtitleP.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.Margin.NONE);
        subtitleP.getStyle().set("color", "#999");

        card.add(titleH3, valueSpan, subtitleP);
        card.setSpacing(false);
        card.setPadding(true);

        return card;
    }

    private VerticalLayout createQuickActionsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(true);
        section.addClassNames(LumoUtility.Margin.Top.LARGE);

        H3 sectionTitle = new H3("⚡ Actions Rapides");
        sectionTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.Bottom.MEDIUM);
        section.add(sectionTitle);

        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setWidthFull();
        actionsLayout.setSpacing(true);
        actionsLayout.getStyle().set("flex-wrap", "wrap");

        // Quick action cards
        actionsLayout.add(createQuickActionCard("Gérer les Utilisateurs", "admin/users", VaadinIcon.USERS, "#667eea"));
        actionsLayout.add(createQuickActionCard("Tous les Événements", "admin/events", VaadinIcon.CALENDAR_CLOCK, "#764ba2"));
        actionsLayout.add(createQuickActionCard("Toutes les Réservations", "admin/reservations", VaadinIcon.RECORDS, "#f093fb"));

        section.add(actionsLayout);
        return section;
    }

    private VerticalLayout createQuickActionCard(String title, String route, VaadinIcon icon, String color) {
        VerticalLayout card = new VerticalLayout();
        card.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.BoxShadow.SMALL
        );
        card.getStyle()
                .set("min-width", "200px")
                .set("flex", "1")
                .set("cursor", "pointer")
                .set("transition", "transform 0.2s, box-shadow 0.2s");

        // Hover effect
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle()
                    .set("transform", "translateY(-4px)")
                    .set("box-shadow", "0 8px 16px rgba(0,0,0,0.15)");
        });
        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");
        });

        Icon actionIcon = icon.create();
        actionIcon.setSize("32px");
        actionIcon.setColor(color);

        Paragraph titleP = new Paragraph(title);
        titleP.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.FontWeight.SEMIBOLD, LumoUtility.Margin.NONE);
        titleP.getStyle().set("color", "#333");

        card.add(actionIcon, titleP);
        card.setAlignItems(Alignment.CENTER);
        card.setSpacing(true);

        // Navigate on click
        card.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(route)));

        return card;
    }

    private VerticalLayout createRecentActivitySection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(true);
        section.addClassNames(LumoUtility.Margin.Top.LARGE);

        H3 sectionTitle = new H3("📋 Activité Récente");
        sectionTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.Bottom.MEDIUM);
        section.add(sectionTitle);

        // Recent events
        List<Event> recentEvents = eventRepository.findAll().stream()
                .sorted((e1, e2) -> e2.getDateCreation().compareTo(e1.getDateCreation()))
                .limit(5)
                .collect(Collectors.toList());

        VerticalLayout eventsList = new VerticalLayout();
        eventsList.setSpacing(true);
        eventsList.setPadding(false);

        if (recentEvents.isEmpty()) {
            Paragraph noEvents = new Paragraph("Aucun événement récent");
            noEvents.getStyle().set("color", "#999");
            eventsList.add(noEvents);
        } else {
            recentEvents.forEach(event -> {
                HorizontalLayout eventItem = new HorizontalLayout();
                eventItem.setWidthFull();
                eventItem.setSpacing(true);
                eventItem.setAlignItems(Alignment.CENTER);
                eventItem.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.BorderRadius.SMALL);
                eventItem.getStyle()
                        .set("background", "#f8f9fa")
                        .set("border-left", "3px solid " + event.getCategorie().getColor());

                Span eventTitle = new Span(event.getTitre());
                eventTitle.addClassNames(LumoUtility.FontWeight.SEMIBOLD);
                Span eventStatus = new Span(event.getStatut().toString());
                eventStatus.getStyle()
                        .set("padding", "4px 8px")
                        .set("border-radius", "4px")
                        .set("background", event.getStatut() == EventStatus.PUBLIE ? "#4caf50" : "#ff9800")
                        .set("color", "white")
                        .set("font-size", "0.75rem");

                eventItem.add(eventTitle, eventStatus);
                eventItem.expand(eventTitle);
                eventsList.add(eventItem);
            });
        }

        section.add(eventsList);
        return section;
    }
}