package com.eventmanager.view.organizer;

import com.eventmanager.entity.Event;
import com.eventmanager.entity.User;
import com.eventmanager.enums.EventStatus;
import com.eventmanager.security.AuthenticatedUser;
import com.eventmanager.service.IEventService;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Route(value = "organizer/dashboard", layout = MainLayout.class)
@PageTitle("Dashboard Organisateur")
public class OrganizerDashboardView extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;
    private final IEventService eventService;

    public OrganizerDashboardView(AuthenticatedUser authenticatedUser, IEventService eventService) {
        this.authenticatedUser = authenticatedUser;
        this.eventService = eventService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        User organizer = authenticatedUser.get()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        H2 title = new H2("📊 Tableau de Bord Organisateur");
        title.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.Margin.Bottom.LARGE);
        add(title);

        List<Event> myEvents = eventService.getEventsByOrganizer(organizer.getId());

        add(buildStatsRow(myEvents));
        add(buildUpcomingGrid(myEvents));
    }

    private HorizontalLayout buildStatsRow(List<Event> events) {
        long total = events.size();
        long published = events.stream().filter(e -> e.getStatut() == EventStatus.PUBLIE).count();
        long draft = events.stream().filter(e -> e.getStatut() == EventStatus.BROUILLON).count();
        long cancelled = events.stream().filter(e -> e.getStatut() == EventStatus.ANNULE).count();

        long upcoming = events.stream()
                .filter(e -> e.getDateDebut() != null && e.getDateDebut().isAfter(LocalDateTime.now()))
                .count();

        HorizontalLayout row = new HorizontalLayout(
                statCard("Total événements", String.valueOf(total)),
                statCard("Publiés", String.valueOf(published)),
                statCard("Brouillons", String.valueOf(draft)),
                statCard("Annulés", String.valueOf(cancelled)),
                statCard("À venir", String.valueOf(upcoming))
        );
        row.setWidthFull();
        row.setSpacing(true);
        row.getStyle().set("flex-wrap", "wrap");
        return row;
    }

    private VerticalLayout statCard(String label, String value) {
        H3 v = new H3(value);
        v.addClassNames(LumoUtility.Margin.NONE);

        Span l = new Span(label);
        l.addClassNames(LumoUtility.TextColor.SECONDARY);

        VerticalLayout card = new VerticalLayout(v, l);
        card.addClassNames(
                LumoUtility.Padding.MEDIUM,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.BoxShadow.SMALL
        );
        card.getStyle().set("min-width", "170px");
        card.setSpacing(false);
        return card;
    }

    private VerticalLayout buildUpcomingGrid(List<Event> events) {
        VerticalLayout container = new VerticalLayout();
        container.setPadding(false);

        H3 subtitle = new H3("📅 Prochains événements");
        subtitle.addClassNames(LumoUtility.Margin.Top.LARGE);

        Grid<Event> grid = new Grid<>(Event.class, false);
        grid.setWidthFull();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        grid.addColumn(Event::getTitre).setHeader("Titre").setAutoWidth(true);
        grid.addColumn(e -> e.getVille() + " - " + e.getLieu()).setHeader("Lieu").setAutoWidth(true);
        grid.addColumn(e -> e.getDateDebut() != null ? e.getDateDebut().format(fmt) : "—")
                .setHeader("Début").setAutoWidth(true);
        grid.addColumn(e -> e.getStatut() != null ? e.getStatut().name() : "—")
                .setHeader("Statut").setAutoWidth(true);

        List<Event> upcoming = events.stream()
                .filter(e -> e.getDateDebut() != null && e.getDateDebut().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Event::getDateDebut))
                .limit(8)
                .toList();

        grid.setItems(upcoming);

        if (upcoming.isEmpty()) {
            Notification.show("Aucun événement à venir pour le moment.");
        }

        container.add(subtitle, grid);
        return container;
    }
}
