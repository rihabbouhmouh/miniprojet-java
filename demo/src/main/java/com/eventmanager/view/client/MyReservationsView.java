package com.eventmanager.view.client;

import com.eventmanager.entity.Reservation;
import com.eventmanager.entity.User;
import com.eventmanager.enums.ReservationStatus;
import com.eventmanager.security.AuthenticatedUser;
import com.eventmanager.service.IReservationService;
import com.eventmanager.view.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "my-reservations", layout = MainLayout.class)
@PageTitle("Mes Réservations")
@CssImport("./styles/my-reservations-view.css")
public class MyReservationsView extends VerticalLayout implements BeforeEnterObserver {

    private final IReservationService reservationService;
    private final AuthenticatedUser authenticatedUser;

    private User currentUser;

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // UI
    private Grid<Reservation> grid;
    private ListDataProvider<Reservation> dataProvider;

    private TextField searchField;
    private ComboBox<ReservationStatus> statusFilter;

    private Span countValue;

    public MyReservationsView(IReservationService reservationService,
                              AuthenticatedUser authenticatedUser) {
        this.reservationService = reservationService;
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassName("mr-root");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!authenticatedUser.isAuthenticated()) {
            Notification.show("Veuillez vous connecter.", 2500, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            event.forwardTo("login");
            return;
        }

        this.currentUser = authenticatedUser.get().orElse(null);
        if (currentUser == null) {
            event.forwardTo("login");
            return;
        }

        buildUI();
        reload();
    }

    private void buildUI() {
        removeAll();

        // ----- Header -----
        Div header = new Div();
        header.addClassName("mr-header");

        H2 title = new H2("Mes réservations");
        title.addClassName("mr-title");

        Span subtitle = new Span("Gérez vos réservations, filtrez par statut, recherchez par code.");
        subtitle.addClassName("mr-subtitle");

        header.add(title, subtitle);

        // ----- Toolbar -----
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.END);
        toolbar.addClassName("mr-toolbar");

        searchField = new TextField("Recherche");
        searchField.setPlaceholder("Code réservation (ex: EVT-AB12CD34)");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setClearButtonVisible(true);
        searchField.setWidth("360px");
        searchField.addValueChangeListener(e -> applyFilters());
        searchField.addKeyPressListener(Key.ENTER, e -> applyFilters());

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setPlaceholder("Tous");
        statusFilter.setClearButtonVisible(true);
        statusFilter.setWidth("220px");
        statusFilter.addValueChangeListener(e -> applyFilters());

        Button refreshBtn = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshBtn.addClickListener(e -> reload());

        countValue = new Span("—");
        countValue.addClassName("mr-count");

        toolbar.add(searchField, statusFilter, refreshBtn);
        toolbar.expand(searchField);

        // ----- Card wrapper -----
        VerticalLayout card = new VerticalLayout();
        card.setWidthFull();
        card.addClassName("mr-card");
        card.setPadding(false);
        card.setSpacing(false);

        // ----- Grid -----
        grid = new Grid<>(Reservation.class, false);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.addClassName("mr-grid");

        grid.addColumn(r -> safe(r.getCodeReservation()))
                .setHeader("Code")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(r -> r.getEvenement() != null ? safe(r.getEvenement().getTitre()) : "—")
                .setHeader("Événement")
                .setFlexGrow(2)
                .setResizable(true);

        grid.addComponentColumn(this::dateCell)
                .setHeader("Date")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(r -> r.getNombrePlaces() != null ? r.getNombrePlaces() : 0)
                .setHeader("Places")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addColumn(r -> formatMoney(r.getMontantTotal()))
                .setHeader("Montant")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addComponentColumn(this::statusBadge)
                .setHeader("Statut")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.addComponentColumn(this::actionsCell)
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // Visual indicator via row class:
        grid.setClassNameGenerator(r -> {
            if (r == null) return "";
            if (r.getStatut() == ReservationStatus.ANNULEE) return "mr-row-cancelled";
            if (isUpcoming(r)) return "mr-row-upcoming";
            return "mr-row-normal";
        });

        // Footer mini bar
        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setAlignItems(Alignment.CENTER);
        footer.addClassName("mr-footer");

        Icon dot = VaadinIcon.CIRCLE.create();
        dot.addClassName("mr-dot");

        Span legend = new Span("À venir");
        legend.addClassName("mr-legend");

        footer.add(dot, legend, new Div(), countValue);
        footer.expand(footer.getComponentAt(2));

        card.add(grid, footer);

        // ----- Page layout -----
        VerticalLayout page = new VerticalLayout(header, toolbar, card);
        page.setSizeFull();
        page.setPadding(true);
        page.setSpacing(true);
        page.addClassNames(LumoUtility.Padding.LARGE, LumoUtility.Gap.MEDIUM);
        add(page);
        expand(page);
    }

    private Component dateCell(Reservation r) {
        HorizontalLayout box = new HorizontalLayout();
        box.setAlignItems(Alignment.CENTER);
        box.setSpacing(true);

        boolean upcoming = isUpcoming(r);

        Icon icon = upcoming ? VaadinIcon.CALENDAR_CLOCK.create() : VaadinIcon.CALENDAR.create();
        icon.addClassName(upcoming ? "mr-icon-upcoming" : "mr-icon");

        String when = "—";
        if (r.getEvenement() != null && r.getEvenement().getDateDebut() != null) {
            when = r.getEvenement().getDateDebut().format(dateFmt);
        }

        Span text = new Span(when);
        text.addClassName("mr-date");

        if (upcoming) {
            Span badge = new Span("À venir");
            badge.addClassName("mr-pill-upcoming");
            box.add(icon, text, badge);
        } else {
            box.add(icon, text);
        }

        return box;
    }

    private Component statusBadge(Reservation r) {
        ReservationStatus st = r.getStatut();
        String label = (st != null ? st.name() : "—");

        Span pill = new Span(label);
        pill.addClassName("mr-pill");

        if (st == ReservationStatus.CONFIRMEE) pill.addClassName("mr-pill-ok");
        else if (st == ReservationStatus.EN_ATTENTE) pill.addClassName("mr-pill-warn");
        else if (st == ReservationStatus.ANNULEE) pill.addClassName("mr-pill-muted");
        else pill.addClassName("mr-pill-muted");

        return pill;
    }

    private Component actionsCell(Reservation r) {
        Button details = new Button("Détails", VaadinIcon.EYE.create());
        details.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        details.addClassName("mr-action");
        details.addClickListener(e -> openDetailsDialog(r));

        Button cancel = new Button("Annuler", VaadinIcon.CLOSE_CIRCLE.create());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        cancel.addClassName("mr-action");

        boolean canCancel = canCancel(r);
        cancel.setEnabled(canCancel);

        cancel.addClickListener(e -> openCancelConfirm(r));

        HorizontalLayout row = new HorizontalLayout(details, cancel);
        row.setSpacing(true);
        row.setPadding(false);
        row.addClassName("mr-actions");
        return row;
    }

    private void openDetailsDialog(Reservation r) {
        Dialog dialog = new Dialog();
        dialog.addClassName("mr-dialog");
        dialog.setHeaderTitle("Détails de la réservation");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        content.add(infoLine("Code", safe(r.getCodeReservation())));
        content.add(infoLine("Statut", r.getStatut() != null ? r.getStatut().name() : "—"));
        content.add(infoLine("Places", String.valueOf(Optional.ofNullable(r.getNombrePlaces()).orElse(0))));
        content.add(infoLine("Montant", formatMoney(r.getMontantTotal())));
        content.add(infoLine("Date réservation",
                r.getDateReservation() != null ? r.getDateReservation().format(dateFmt) : "—"));

        if (r.getEvenement() != null) {
            content.add(new Hr());
            content.add(sectionTitle("Événement"));
            content.add(infoLine("Titre", safe(r.getEvenement().getTitre())));
            content.add(infoLine("Date",
                    r.getEvenement().getDateDebut() != null ? r.getEvenement().getDateDebut().format(dateFmt) : "—"));
            content.add(infoLine("Lieu", safe(r.getEvenement().getLieu()) + " - " + safe(r.getEvenement().getVille())));
            content.add(infoLine("Prix unitaire",
                    r.getEvenement().getPrixUnitaire() != null ? String.format("%.2f DH", r.getEvenement().getPrixUnitaire()) : "—"));
        }

        Button goToEvent = new Button("Voir l’événement", VaadinIcon.ARROW_RIGHT.create());
        goToEvent.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        goToEvent.addClickListener(e -> {
            dialog.close();
            if (r.getEvenement() != null) {
                getUI().ifPresent(ui -> ui.navigate("event/" + r.getEvenement().getId()));
            }
        });

        Button close = new Button("Fermer");
        close.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        close.addClickListener(e -> dialog.close());

        HorizontalLayout actions = new HorizontalLayout(goToEvent, close);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);

        dialog.add(content, actions);
        dialog.open();
    }

    private void openCancelConfirm(Reservation r) {
        Dialog dialog = new Dialog();
        dialog.addClassName("mr-dialog");
        dialog.setHeaderTitle("Annuler la réservation");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        Span text = new Span("Confirmer l’annulation de la réservation " + safe(r.getCodeReservation()) + " ?");
        text.addClassName("mr-danger-text");
        content.add(text);

        if (!canCancel(r)) {
            Span hint = new Span("Cette réservation ne peut plus être annulée.");
            hint.addClassName("mr-muted");
            content.add(hint);
        }

        Button confirm = new Button("Annuler la réservation", VaadinIcon.TRASH.create());
        confirm.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        confirm.setEnabled(canCancel(r));
        confirm.addClickListener(e -> {
            try {
                reservationService.cancelReservation(r.getId());
                dialog.close();
                Notification.show("Réservation annulée.", 2500, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                reload();
            } catch (Exception ex) {
                dialog.close();
                Notification.show(ex.getMessage() != null ? ex.getMessage() : "Erreur lors de l’annulation",
                                3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button close = new Button("Fermer");
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        close.addClickListener(e -> dialog.close());

        HorizontalLayout actions = new HorizontalLayout(close, confirm);
        actions.setWidthFull();
        actions.setJustifyContentMode(JustifyContentMode.END);

        dialog.add(content, actions);
        dialog.open();
    }

    private Component sectionTitle(String text) {
        H4 t = new H4(text);
        t.addClassName("mr-section");
        return t;
    }

    private Component infoLine(String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(Alignment.BASELINE);
        row.addClassName("mr-info-row");

        Span l = new Span(label);
        l.addClassName("mr-info-label");

        Span v = new Span(value);
        v.addClassName("mr-info-value");

        row.add(l, v);
        row.expand(v);
        return row;
    }

    private void reload() {
        List<Reservation> items;
        try {
            items = reservationService.getReservationsByUser(currentUser.getId());
        } catch (Exception ex) {
            items = new ArrayList<>();
        }

        // Sort: upcoming first, then most recent
        items.sort(Comparator
                .comparing((Reservation r) -> !isUpcoming(r)) // false first => upcoming first
                .thenComparing((Reservation r) -> {
                    if (r.getEvenement() != null) return r.getEvenement().getDateDebut();
                    return null;
                }, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Reservation::getDateReservation, Comparator.nullsLast(Comparator.reverseOrder()))
        );

        if (dataProvider == null) {
            dataProvider = new ListDataProvider<>(items);
            grid.setDataProvider(dataProvider);
        } else {
            dataProvider.getItems().clear();
            dataProvider.getItems().addAll(items);
            dataProvider.refreshAll();
        }

        applyFilters();
    }

    private void applyFilters() {
        if (dataProvider == null) return;

        String q = searchField != null ? searchField.getValue() : null;
        ReservationStatus status = statusFilter != null ? statusFilter.getValue() : null;

        String query = q != null ? q.trim().toLowerCase() : "";

        dataProvider.clearFilters();

        dataProvider.addFilter(r -> {
            boolean okCode = true;
            if (!query.isEmpty()) {
                okCode = safe(r.getCodeReservation()).toLowerCase().contains(query);
            }

            boolean okStatus = true;
            if (status != null) {
                okStatus = r.getStatut() == status;
            }

            return okCode && okStatus;
        });

        int count = dataProvider.getItems().stream()
                .filter(r -> {
                    boolean okCode = query.isEmpty() || safe(r.getCodeReservation()).toLowerCase().contains(query);
                    boolean okStatus = status == null || r.getStatut() == status;
                    return okCode && okStatus;
                })
                .collect(Collectors.toList()).size();

        countValue.setText(count + " réservation(s)");
    }

    /**
     * “À venir” indicator: event date in the future and not cancelled.
     */
    private boolean isUpcoming(Reservation r) {
        if (r == null) return false;
        if (r.getStatut() == ReservationStatus.ANNULEE) return false;
        if (r.getEvenement() == null || r.getEvenement().getDateDebut() == null) return false;
        return r.getEvenement().getDateDebut().isAfter(LocalDateTime.now());
    }

    /**
     * UI rule for cancel button.
     * Your service already blocks cancelling after the event has started.
     * If you want the “48h before” rule, keep the +48h check below (recommended).
     */
    private boolean canCancel(Reservation r) {
        if (r == null) return false;
        if (r.getId() == null) return false;
        if (r.getStatut() == ReservationStatus.ANNULEE) return false;

        if (r.getEvenement() == null || r.getEvenement().getDateDebut() == null) return false;

        LocalDateTime start = r.getEvenement().getDateDebut();
        LocalDateTime now = LocalDateTime.now();

        // must be in the future
        if (!start.isAfter(now)) return false;

        // Optional: 48h rule
        return start.isAfter(now.plusHours(48));
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }

    private String formatMoney(Double amount) {
        double v = amount != null ? amount : 0.0;
        return String.format("%.2f DH", v);
    }
}
