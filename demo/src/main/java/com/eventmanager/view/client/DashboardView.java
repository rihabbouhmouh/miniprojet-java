package com.eventmanager.view.client;

import com.eventmanager.entity.Event;
import com.eventmanager.entity.Reservation;
import com.eventmanager.entity.User;
import com.eventmanager.enums.ReservationStatus;
import com.eventmanager.repository.ReservationRepository;
import com.eventmanager.security.AuthenticatedUser;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard")
public class DashboardView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthenticatedUser authenticatedUser;
    private final ReservationRepository reservationRepository;

    private User currentUser;

    // Stats values
    private Span reservationsCount;
    private Span upcomingCount;
    private Span totalSpent;
    private Span pendingCount;

    // Notifications box (fixed: no DOM owner lookup)
    private Div notifBox;

    // Upcoming grid
    private Grid<Reservation> upcomingGrid;

    public DashboardView(AuthenticatedUser authenticatedUser,
                         ReservationRepository reservationRepository) {
        this.authenticatedUser = authenticatedUser;
        this.reservationRepository = reservationRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassNames(LumoUtility.Padding.LARGE, LumoUtility.Gap.MEDIUM);
        getStyle().set("background", "var(--lumo-base-color)");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.currentUser = authenticatedUser.get().orElse(null);

        if (currentUser == null) {
            Notification.show("Please login to access the dashboard.",
                            2500, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            event.forwardTo("login");
            return;
        }

        buildUI();
        loadData();
    }

    private void buildUI() {
        removeAll();

        // Page wrapper
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setWidthFull();
        wrapper.setMaxWidth("1100px");
        wrapper.getStyle().set("margin", "0 auto");
        wrapper.setPadding(false);
        wrapper.setSpacing(true);

        // Header
        H2 title = new H2("Welcome, " + safe(currentUser.getPrenom(), "Client"));
        title.getStyle().set("margin", "0");

        Span subtitle = new Span("Your account overview");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY);
        subtitle.getStyle().set("margin-bottom", "10px");

        wrapper.add(title, subtitle);

        // Stats row
        reservationsCount = new Span("—");
        upcomingCount = new Span("—");
        totalSpent = new Span("—");
        pendingCount = new Span("—");

        HorizontalLayout statsRow = new HorizontalLayout(
                statCard("Reservations", reservationsCount, "Total reservations"),
                statCard("Upcoming", upcomingCount, "Reserved upcoming events"),
                statCard("Spent", totalSpent, "Confirmed total"),
                statCard("Pending", pendingCount, "Waiting confirmation")
        );
        statsRow.setWidthFull();
        statsRow.setSpacing(true);
        statsRow.getStyle().set("flex-wrap", "wrap");

        wrapper.add(statsRow);

        // Shortcuts
        wrapper.add(sectionTitle("Shortcuts"));

        HorizontalLayout shortcuts = new HorizontalLayout();
        shortcuts.setWidthFull();
        shortcuts.setSpacing(true);
        shortcuts.getStyle().set("flex-wrap", "wrap");

        shortcuts.add(
                shortcutButton("Browse events", VaadinIcon.SEARCH, "events"),
                shortcutButton("My reservations", VaadinIcon.TICKET, "my-reservations"),
                shortcutButton("My profile", VaadinIcon.USER, "profile")
        );

        wrapper.add(shortcuts);

        // Notifications (black/white)
        wrapper.add(sectionTitle("Notifications"));

        notifBox = new Div();
        notifBox.setWidthFull();
        notifBox.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "12px")
                .set("padding", "12px 14px")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        wrapper.add(notifBox);

        // Upcoming grid
        wrapper.add(sectionTitle("Upcoming reservations"));

        upcomingGrid = new Grid<>(Reservation.class, false);
        upcomingGrid.setWidthFull();
        upcomingGrid.setHeight(360, Unit.PIXELS);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        upcomingGrid.addColumn(r -> safe(r.getCodeReservation(), "—"))
                .setHeader("Code").setAutoWidth(true);

        upcomingGrid.addColumn(r -> {
                    Event ev = r.getEvenement();
                    return ev != null ? safe(ev.getTitre(), "—") : "—";
                })
                .setHeader("Event").setAutoWidth(true).setFlexGrow(1);

        upcomingGrid.addColumn(r -> {
                    Event ev = r.getEvenement();
                    if (ev == null || ev.getDateDebut() == null) return "—";
                    return ev.getDateDebut().format(fmt);
                })
                .setHeader("Start").setAutoWidth(true);

        upcomingGrid.addColumn(r -> r.getNombrePlaces() != null ? r.getNombrePlaces() : 0)
                .setHeader("Seats").setAutoWidth(true);

        upcomingGrid.addComponentColumn(this::statusPill)
                .setHeader("Status").setAutoWidth(true);

        upcomingGrid.addComponentColumn(r -> {
            Button view = new Button("View", VaadinIcon.EYE.create());
            view.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            view.getStyle().set("border-radius", "10px");
            view.addClickListener(e -> {
                Long eventId = r.getEvenement() != null ? r.getEvenement().getId() : null;
                if (eventId != null) {
                    getUI().ifPresent(ui -> ui.navigate("event/" + eventId));
                }
            });
            return view;
        }).setHeader("").setAutoWidth(true).setFlexGrow(0);

        // B/W grid styling container
        Div gridCard = new Div(upcomingGrid);
        gridCard.setWidthFull();
        gridCard.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "12px")
                .set("padding", "8px")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        wrapper.add(gridCard);

        add(wrapper);
    }

    private void loadData() {
        // Uses your existing method to avoid lazy-loading issues in Vaadin grids
        List<Reservation> all = reservationRepository.findAllWithDetails();

        Long userId = currentUser.getId();
        LocalDateTime now = LocalDateTime.now();

        List<Reservation> myReservations = all.stream()
                .filter(r -> r.getUtilisateur() != null && r.getUtilisateur().getId().equals(userId))
                .toList();

        long totalRes = myReservations.size();

        long pending = myReservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.EN_ATTENTE)
                .count();

        double spent = myReservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .mapToDouble(r -> r.getMontantTotal() != null ? r.getMontantTotal() : 0.0)
                .sum();

        List<Reservation> upcoming = myReservations.stream()
                .filter(r -> r.getStatut() != ReservationStatus.ANNULEE)
                .filter(r -> r.getEvenement() != null && r.getEvenement().getDateDebut() != null)
                .filter(r -> r.getEvenement().getDateDebut().isAfter(now))
                .sorted(Comparator.comparing(r -> r.getEvenement().getDateDebut()))
                .limit(8)
                .toList();

        long upcomingEvents = upcoming.size();

        reservationsCount.setText(String.valueOf(totalRes));
        upcomingCount.setText(String.valueOf(upcomingEvents));
        totalSpent.setText(String.format("%.2f DH", spent));
        pendingCount.setText(String.valueOf(pending));

        upcomingGrid.setItems(upcoming);

        // Notifications (fixed: use field reference, no DOM lookup)
        notifBox.removeAll();

        if (pending > 0) {
            notifBox.add(miniNotice(pending + " reservation(s) pending confirmation."));
        }

        if (upcomingEvents == 0) {
            notifBox.add(miniNotice("No upcoming events at the moment."));
        }

        if (pending == 0 && upcomingEvents > 0) {
            notifBox.add(miniNotice("All good. Your upcoming reservations are listed below."));
        }

        if (pending == 0 && upcomingEvents == 0) {
            notifBox.add(miniNotice("No alerts. You can browse events and book your next one."));
        }
    }

    // ---------- UI helpers (black & white) ----------

    private Component sectionTitle(String text) {
        H3 h3 = new H3(text);
        h3.getStyle().set("margin", "16px 0 6px 0");
        return h3;
    }

    private VerticalLayout statCard(String title, Span value, String hint) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.setWidth("260px");

        card.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "12px")
                .set("background", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        Span t = new Span(title);
        t.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        value.getStyle()
                .set("display", "block")
                .set("font-size", "2.2em")
                .set("font-weight", "800")
                .set("margin-top", "6px");

        Span small = new Span(hint);
        small.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
        small.getStyle().set("margin-top", "6px");

        card.add(t, value, small);
        return card;
    }

    private Button shortcutButton(String text, VaadinIcon icon, String route) {
        Button b = new Button(text, icon.create());
        b.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        b.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "12px")
                .set("background", "var(--lumo-base-color)");
        b.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(route)));
        return b;
    }

    private Span statusPill(Reservation r) {
        ReservationStatus st = r.getStatut();
        String label = st != null ? st.name() : "—";

        Span pill = new Span(label);
        pill.getStyle()
                .set("padding", "6px 10px")
                .set("border-radius", "999px")
                .set("border", "1px solid var(--lumo-contrast-30pct)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("font-weight", "700")
                .set("font-size", "0.85em");
        return pill;
    }

    private Div miniNotice(String text) {
        Div d = new Div();
        d.setText(text);
        d.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "10px")
                .set("padding", "10px 12px")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("color", "var(--lumo-body-text-color)")
                .set("margin-bottom", "8px")
                .set("font-weight", "600");
        return d;
    }

    private String safe(String v, String fallback) {
        return (v == null || v.isBlank()) ? fallback : v;
    }
}
