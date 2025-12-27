package com.eventmanager.view.organizer;

import com.eventmanager.entity.Event;
import com.eventmanager.entity.User;
import com.eventmanager.enums.EventStatus;
import com.eventmanager.security.AuthenticatedUser;
import com.eventmanager.service.IEventService;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
        setSpacing(false);

        getStyle()
                .set("background", "#f5f7fa")
                .set("padding", "24px");

        User organizer = authenticatedUser.get()
                .orElseThrow(() -> new IllegalStateException("Utilisateur non authentifié"));

        // Page container (centered like your Home sections)
        Div page = new Div();
        page.getStyle()
                .set("max-width", "1200px")
                .set("margin", "0 auto")
                .set("width", "100%");

        page.add(buildHeader(organizer));

        List<Event> myEvents = eventService.getEventsByOrganizer(organizer.getId());

        page.add(buildStatsRow(myEvents));
        page.add(buildUpcomingSection(myEvents));

        add(page);
    }

    private Div buildHeader(User organizer) {
        Div header = new Div();
        header.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "6px")
                .set("margin-bottom", "18px");

        H2 title = new H2("📊 Tableau de Bord Organisateur");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "2rem")
                .set("font-weight", "800")
                .set("color", "#111827");

        Paragraph subtitle = new Paragraph("Bonjour " + organizer.getPrenom() + " 👋 Suivez vos événements et vos statuts en un coup d’œil.");
        subtitle.getStyle()
                .set("margin", "0")
                .set("color", "#6b7280");

        header.add(title, subtitle);
        return header;
    }

    private Div buildStatsRow(List<Event> events) {
        long total = events.size();
        long published = events.stream().filter(e -> e.getStatut() == EventStatus.PUBLIE).count();
        long draft = events.stream().filter(e -> e.getStatut() == EventStatus.BROUILLON).count();
        long cancelled = events.stream().filter(e -> e.getStatut() == EventStatus.ANNULE).count();
        long upcoming = events.stream()
                .filter(e -> e.getDateDebut() != null && e.getDateDebut().isAfter(LocalDateTime.now()))
                .count();

        Div grid = new Div();
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fit, minmax(200px, 1fr))")
                .set("gap", "14px")
                .set("margin", "18px 0 26px 0");

        grid.add(statCard("Total événements", String.valueOf(total), "#667eea"));
        grid.add(statCard("Publiés", String.valueOf(published), "#22c55e"));
        grid.add(statCard("Brouillons", String.valueOf(draft), "#f59e0b"));
        grid.add(statCard("Annulés", String.valueOf(cancelled), "#ef4444"));
        grid.add(statCard("À venir", String.valueOf(upcoming), "#8b5cf6"));

        return grid;
    }

    private Div statCard(String label, String value, String accent) {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "14px")
                .set("box-shadow", "0 10px 30px rgba(0,0,0,0.06)")
                .set("padding", "16px 16px")
                .set("border", "1px solid rgba(17,24,39,0.06)")
                .set("position", "relative")
                .set("overflow", "hidden");

        // left accent line
        Div line = new Div();
        line.getStyle()
                .set("position", "absolute")
                .set("left", "0")
                .set("top", "0")
                .set("bottom", "0")
                .set("width", "5px")
                .set("background", accent);

        Span v = new Span(value);
        v.getStyle()
                .set("display", "block")
                .set("font-size", "2rem")
                .set("font-weight", "800")
                .set("color", "#111827")
                .set("line-height", "1");

        Span l = new Span(label);
        l.getStyle()
                .set("display", "block")
                .set("margin-top", "8px")
                .set("color", "#6b7280")
                .set("font-weight", "600");

        card.add(line, v, l);
        return card;
    }

    private Div buildUpcomingSection(List<Event> events) {
        Div section = new Div();
        section.getStyle().set("margin-bottom", "30px");

        // Section header row
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H3 subtitle = new H3("📅 Prochains événements");
        subtitle.getStyle()
                .set("margin", "0")
                .set("font-size", "1.35rem")
                .set("font-weight", "800")
                .set("color", "#111827");

        Span hint = new Span("Les 8 prochains événements à venir");
        hint.getStyle()
                .set("color", "#6b7280")
                .set("font-size", "0.95rem");

        header.add(subtitle, hint);

        // Card container for the grid
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "14px")
                .set("box-shadow", "0 10px 30px rgba(0,0,0,0.06)")
                .set("border", "1px solid rgba(17,24,39,0.06)")
                .set("padding", "14px")
                .set("margin-top", "12px");

        Grid<Event> grid = new Grid<>(Event.class, false);
        grid.setWidthFull();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        grid.addColumn(Event::getTitre)
                .setHeader("Titre")
                .setAutoWidth(true)
                .setFlexGrow(2);

        grid.addColumn(e -> safe(e.getVille()) + " - " + safe(e.getLieu()))
                .setHeader("Lieu")
                .setAutoWidth(true)
                .setFlexGrow(2);

        grid.addColumn(e -> e.getDateDebut() != null ? e.getDateDebut().format(fmt) : "—")
                .setHeader("Début")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addComponentColumn(this::statusPill)
                .setHeader("Statut")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // IMPORTANT: more rows visible (fixes "only 2 rows" feeling)
        grid.setAllRowsVisible(true);

        List<Event> upcoming = events.stream()
                .filter(e -> e.getDateDebut() != null && e.getDateDebut().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Event::getDateDebut))
                .limit(8)
                .toList();

        if (upcoming.isEmpty()) {
            Div empty = new Div();
            empty.getStyle()
                    .set("padding", "20px")
                    .set("text-align", "center")
                    .set("color", "#6b7280");
            empty.setText("Aucun événement à venir pour le moment.");
            card.add(empty);
        } else {
            grid.setItems(upcoming);
            card.add(grid);
        }

        section.add(header, card);
        return section;
    }

    private Span statusPill(Event event) {
        String text = event.getStatut() != null ? event.getStatut().name() : "—";
        Span pill = new Span(text);

        String bg;
        String color = "#111827";

        if (event.getStatut() == EventStatus.PUBLIE) {
            bg = "rgba(34,197,94,0.15)";
            color = "#16a34a";
        } else if (event.getStatut() == EventStatus.BROUILLON) {
            bg = "rgba(245,158,11,0.16)";
            color = "#b45309";
        } else if (event.getStatut() == EventStatus.ANNULE) {
            bg = "rgba(239,68,68,0.15)";
            color = "#dc2626";
        } else if (event.getStatut() == EventStatus.TERMINE) {
            bg = "rgba(107,114,128,0.18)";
            color = "#374151";
        } else {
            bg = "rgba(102,126,234,0.12)";
            color = "#4f46e5";
        }

        pill.getStyle()
                .set("padding", "6px 10px")
                .set("border-radius", "999px")
                .set("font-weight", "700")
                .set("font-size", "0.8rem")
                .set("background", bg)
                .set("color", color)
                .set("border", "1px solid rgba(17,24,39,0.08)");

        return pill;
    }

    private String safe(String v) {
        return v != null ? v : "—";
    }
}
