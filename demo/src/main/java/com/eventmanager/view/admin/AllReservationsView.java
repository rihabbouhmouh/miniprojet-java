package com.eventmanager.view.admin;

import com.eventmanager.entity.Reservation;
import com.eventmanager.enums.ReservationStatus;
import com.eventmanager.repository.ReservationRepository;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "admin/reservations", layout = MainLayout.class)
@PageTitle("Gestion des Réservations - Admin")
public class AllReservationsView extends VerticalLayout {

    private final ReservationRepository reservationRepository;

    private Grid<Reservation> grid;
    private ListDataProvider<Reservation> dataProvider;

    private TextField searchField;
    private ComboBox<ReservationStatus> statusFilter;

    public AllReservationsView(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("🎫 Gestion de Toutes les Réservations");
        title.addClassNames(LumoUtility.FontSize.XXXLARGE);
        add(title);

        add(createFilters());
        add(createGrid());

        loadReservations();
    }

    /* ================= FILTERS ================= */

    private HorizontalLayout createFilters() {
        searchField = new TextField("Code");
        searchField.setPlaceholder("EVT-XXXXX");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addValueChangeListener(e -> applyFilters());

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> applyFilters());

        Button refresh = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refresh.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refresh.addClickListener(e -> loadReservations());

        HorizontalLayout layout = new HorizontalLayout(searchField, statusFilter, refresh);
        layout.setAlignItems(Alignment.END);
        return layout;
    }

    /* ================= GRID ================= */

    private Grid<Reservation> createGrid() {
        grid = new Grid<>(Reservation.class, false);
        grid.setSizeFull();

        grid.addColumn(Reservation::getId).setHeader("ID").setWidth("70px");

        grid.addColumn(r ->
                r.getCodeReservation() != null ? r.getCodeReservation() : "—"
        ).setHeader("Code");

        grid.addColumn(r ->
                r.getUtilisateur() != null
                        ? r.getUtilisateur().getPrenom() + " " + r.getUtilisateur().getNom()
                        : "—"
        ).setHeader("Client");

        grid.addColumn(r ->
                r.getUtilisateur() != null ? r.getUtilisateur().getEmail() : "—"
        ).setHeader("Email");

        grid.addColumn(r ->
                r.getEvenement() != null ? r.getEvenement().getTitre() : "—"
        ).setHeader("Événement");

        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Places");

        grid.addColumn(r ->
                r.getMontantTotal() != null
                        ? String.format("%.2f DH", r.getMontantTotal())
                        : "—"
        ).setHeader("Montant");

        grid.addColumn(r ->
                r.getDateReservation() != null
                        ? r.getDateReservation()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        : "—"
        ).setHeader("Date");

        grid.addComponentColumn(this::statusBadge)
                .setHeader("Statut");

        grid.addComponentColumn(this::actions)
                .setHeader("Actions");

        return grid;
    }

    /* ================= ACTIONS ================= */

    private HorizontalLayout actions(Reservation r) {
        HorizontalLayout layout = new HorizontalLayout();

        if (r.getStatut() == ReservationStatus.EN_ATTENTE) {
            Button confirm = new Button(VaadinIcon.CHECK.create(), e ->
                    updateStatus(r, ReservationStatus.CONFIRMEE));
            confirm.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            layout.add(confirm);
        }

        if (r.getStatut() != ReservationStatus.ANNULEE) {
            Button cancel = new Button(VaadinIcon.CLOSE.create(), e ->
                    updateStatus(r, ReservationStatus.ANNULEE));
            cancel.addThemeVariants(ButtonVariant.LUMO_ERROR);
            layout.add(cancel);
        }

        if (r.getEvenement() != null) {
            Button view = new Button(VaadinIcon.EYE.create(), e ->
                    getUI().ifPresent(ui ->
                            ui.navigate("event/" + r.getEvenement().getId())));
            layout.add(view);
        }

        return layout;
    }

    private Span statusBadge(Reservation r) {
        Span badge = new Span(r.getStatut().name());
        badge.getStyle()
                .set("padding", "4px 8px")
                .set("border-radius", "4px")
                .set("color", "white")
                .set("background",
                        r.getStatut() == ReservationStatus.CONFIRMEE ? "#4caf50"
                                : r.getStatut() == ReservationStatus.EN_ATTENTE ? "#ff9800"
                                : "#f44336");
        return badge;
    }

    /* ================= DATA ================= */

    private void loadReservations() {
        List<Reservation> reservations = reservationRepository.findAllWithDetails();
        dataProvider = new ListDataProvider<>(reservations);
        grid.setDataProvider(dataProvider);
    }

    private void applyFilters() {
        dataProvider.clearFilters();

        if (!searchField.isEmpty()) {
            dataProvider.addFilter(r ->
                    r.getCodeReservation() != null &&
                            r.getCodeReservation().toLowerCase()
                                    .contains(searchField.getValue().toLowerCase()));
        }

        if (statusFilter.getValue() != null) {
            dataProvider.addFilter(r ->
                    r.getStatut() == statusFilter.getValue());
        }
    }

    private void updateStatus(Reservation reservation, ReservationStatus status) {
        try {
            reservation.setStatut(status);
            reservationRepository.save(reservation);
            Notification.show("Statut mis à jour",
                    3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadReservations();
        } catch (Exception e) {
            Notification.show(e.getMessage(),
                    5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
