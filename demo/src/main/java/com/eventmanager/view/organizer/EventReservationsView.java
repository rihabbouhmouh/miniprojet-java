package com.eventmanager.view.organizer;

import com.eventmanager.entity.Event;
import com.eventmanager.entity.Reservation;
import com.eventmanager.enums.ReservationStatus;
import com.eventmanager.security.AuthenticatedUser;
import com.eventmanager.service.IEventService;
import com.eventmanager.service.IReservationService;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "organizer/event/:eventId/reservations", layout = MainLayout.class)
@PageTitle("Réservations de l'événement")
public class EventReservationsView extends VerticalLayout implements BeforeEnterObserver {

    private final IReservationService reservationService;
    private final IEventService eventService;

    private Event event;
    private Grid<Reservation> grid;
    private ListDataProvider<Reservation> dataProvider;

    private ComboBox<ReservationStatus> statusFilter;
    private TextField searchField;

    private Span totalReservations;
    private Span totalPlaces;
    private Span totalRevenue;

    public EventReservationsView(IReservationService reservationService,
                                 IEventService eventService) {
        this.reservationService = reservationService;
        this.eventService = eventService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    /* -------------------- ROUTE PARAM -------------------- */

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Long eventId = event.getRouteParameters()
                .get("eventId")
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalArgumentException("ID événement manquant"));

        this.event = eventService.getEventById(eventId);

        buildView();
        loadReservations();
    }

    /* -------------------- UI BUILD -------------------- */

    private void buildView() {
        removeAll();

        H2 title = new H2("🎟 Réservations — " + event.getTitre());
        add(title);

        add(createStatsBar());
        add(createFiltersBar());
        add(createGrid());
    }

    private HorizontalLayout createStatsBar() {
        totalReservations = statCard("Réservations", "0");
        totalPlaces = statCard("Places réservées", "0");
        totalRevenue = statCard("Revenus", "0 DH");

        HorizontalLayout stats = new HorizontalLayout(
                totalReservations, totalPlaces, totalRevenue
        );
        stats.setWidthFull();
        stats.setSpacing(true);
        return stats;
    }

    private Span statCard(String label, String value) {
        Span span = new Span(label + " : " + value);
        span.getStyle()
                .set("padding", "10px")
                .set("border-radius", "6px")
                .set("background", "#f5f5f5")
                .set("font-weight", "600");
        return span;
    }

    private HorizontalLayout createFiltersBar() {
        searchField = new TextField();
        searchField.setPlaceholder("Code réservation ou utilisateur...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addValueChangeListener(e -> applyFilters());

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> applyFilters());

        Button export = new Button("Exporter CSV", VaadinIcon.DOWNLOAD.create());
        export.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        export.addClickListener(e ->
                Notification.show("Export CSV (bonus)", 2000, Notification.Position.TOP_CENTER)
        );

        HorizontalLayout bar = new HorizontalLayout(searchField, statusFilter, export);
        bar.setWidthFull();
        bar.setAlignItems(Alignment.END);
        bar.expand(searchField);
        return bar;
    }

    private Grid<Reservation> createGrid() {
        grid = new Grid<>(Reservation.class, false);
        grid.setSizeFull();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        grid.addColumn(Reservation::getCodeReservation)
                .setHeader("Code").setAutoWidth(true);

        grid.addColumn(r -> r.getUtilisateur().getNom() + " " + r.getUtilisateur().getPrenom())
                .setHeader("Utilisateur").setAutoWidth(true);

        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Places").setAutoWidth(true);

        grid.addColumn(r -> String.format("%.2f DH", r.getMontantTotal()))
                .setHeader("Montant").setAutoWidth(true);

        grid.addColumn(r -> r.getDateReservation().format(fmt))
                .setHeader("Date").setAutoWidth(true);

        grid.addComponentColumn(this::statusBadge)
                .setHeader("Statut").setAutoWidth(true);

        grid.addComponentColumn(this::actionsColumn)
                .setHeader("Actions").setAutoWidth(true);

        return grid;
    }

    /* -------------------- DATA -------------------- */

    private void loadReservations() {
        List<Reservation> reservations =
                reservationService.getReservationsByEvent(event.getId());

        dataProvider = new ListDataProvider<>(reservations);
        grid.setDataProvider(dataProvider);

        updateStats(reservations);
    }

    private void updateStats(List<Reservation> reservations) {
        long count = reservations.size();
        long places = reservations.stream()
                .filter(r -> r.getStatut() != ReservationStatus.ANNULEE)
                .mapToLong(Reservation::getNombrePlaces)
                .sum();

        double revenue = reservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .mapToDouble(Reservation::getMontantTotal)
                .sum();

        totalReservations.setText("Réservations : " + count);
        totalPlaces.setText("Places réservées : " + places);
        totalRevenue.setText("Revenus : " + String.format("%.2f DH", revenue));
    }

    private void applyFilters() {
        if (dataProvider == null) return;
        dataProvider.clearFilters();

        String keyword = searchField.getValue();
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.toLowerCase();
            dataProvider.addFilter(r ->
                    r.getCodeReservation().toLowerCase().contains(k) ||
                    (r.getUtilisateur().getNom() + " " + r.getUtilisateur().getPrenom())
                            .toLowerCase().contains(k)
            );
        }

        if (statusFilter.getValue() != null) {
            dataProvider.addFilter(r -> r.getStatut() == statusFilter.getValue());
        }
    }

    /* -------------------- COMPONENTS -------------------- */

    private Span statusBadge(Reservation r) {
        Span badge = new Span(r.getStatut().name());
        String bg = switch (r.getStatut()) {
            case CONFIRMEE -> "#4caf50";
            case EN_ATTENTE -> "#ff9800";
            case ANNULEE -> "#f44336";
        };
        badge.getStyle()
                .set("padding", "4px 8px")
                .set("border-radius", "4px")
                .set("color", "white")
                .set("background", bg);
        return badge;
    }

    private HorizontalLayout actionsColumn(Reservation r) {
        Button details = new Button(VaadinIcon.EYE.create());
        details.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        details.addClickListener(e ->
                Notification.show(r.getInfoReservation(), 3000, Notification.Position.TOP_CENTER)
        );

        Button confirm = new Button(VaadinIcon.CHECK.create());
        confirm.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY);
        confirm.setEnabled(r.getStatut() == ReservationStatus.EN_ATTENTE);
        confirm.addClickListener(e -> confirmReservation(r));

        return new HorizontalLayout(details, confirm);
    }

    private void confirmReservation(Reservation r) {
        try {
            reservationService.confirmReservation(r.getId());
            Notification.show("Réservation confirmée", 2500, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadReservations();
        } catch (Exception ex) {
            Notification.show(ex.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
